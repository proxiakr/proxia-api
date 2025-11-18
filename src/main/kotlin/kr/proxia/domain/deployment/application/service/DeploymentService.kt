package kr.proxia.domain.deployment.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import kr.proxia.domain.container.domain.entity.ContainerEntity
import kr.proxia.domain.container.domain.repository.ContainerRepository
import kr.proxia.domain.node.application.scheduler.NodeScheduler
import kr.proxia.domain.resource.domain.entity.AppResourceEntity
import kr.proxia.domain.resource.domain.error.ResourceError
import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.service.domain.enums.AppFramework
import kr.proxia.domain.service.domain.error.ServiceError
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.global.container.ContainerOrchestrator
import kr.proxia.global.container.ContainerSpec
import kr.proxia.global.container.DockerfileGenerator
import kr.proxia.global.error.BusinessException
import kr.proxia.global.image.ImageBuilder
import kr.proxia.global.reverseproxy.ReverseProxyAdapter
import org.eclipse.jgit.api.Git
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.io.File
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class DeploymentService(
    private val dockerfileGenerator: DockerfileGenerator,
    private val serviceRepository: ServiceRepository,
    private val appResourceRepository: AppResourceRepository,
    private val containerRepository: ContainerRepository,
    private val nodeScheduler: NodeScheduler,
    private val containerOrchestrator: ContainerOrchestrator,
    private val imageBuilder: ImageBuilder,
    @param:Qualifier("nginxAdapter") private val reverseProxyAdapter: ReverseProxyAdapter,
) {
    private val workDir = File(System.getProperty("java.io.tmpdir", "proxia-deployments"))

    @Async
    @Transactional
    fun deploy(serviceId: UUID) {
        val service =
            serviceRepository.findByIdAndDeletedAtIsNull(serviceId)
                ?: throw BusinessException(ServiceError.SERVICE_NOT_FOUND)

        val node = nodeScheduler.chooseNode()

        logger.info { "Selected node '${node.name}' for service $serviceId" }

        val appResource =
            appResourceRepository
                .findById(service.targetId!!)
                .orElseThrow { BusinessException(ResourceError.RESOURCE_NOT_FOUND) }

        val repoDir = cloneRepository(appResource.repositoryUrl!!, appResource.branch ?: "main", service.id)
        val commitInfo = getLatestCommitInfo(repoDir)
        val dockerfile = findOrCreateDockerfile(repoDir, appResource)

        val imageName = "proxia-service-${service.id}"

        val imageId =
            imageBuilder.buildImage(
                endpoint = node.endpoint,
                contextDir = repoDir,
                dockerfile = dockerfile,
                imageName = imageName,
            )

        val env = parseEnvVariables(appResource.envVariables)

        val containerSpec =
            ContainerSpec(
                name = "svc-${service.id}-${System.currentTimeMillis()}",
                image = "$imageName:latest",
                env = env,
                ports =
                    listOf(
                        ContainerSpec.PortMapping(
                            internal = 8080,
                            host = null,
                        ),
                    ),
            )

        val endpoint = node.endpoint
        val containerId = containerOrchestrator.createContainer(endpoint, containerSpec)

        containerOrchestrator.startContainer(endpoint, containerId)

        val assignedPort = containerOrchestrator.getAssignedPort(endpoint, containerId, 8080)

        containerRepository.save(
            ContainerEntity(
                serviceId = serviceId,
                nodeId = node.id,
                containerId = containerId,
                imageId = imageId,
                port = assignedPort,
                internalPort = 8080,
            ),
        )

        val domain = createOrResolveDomain(serviceId)

        reverseProxyAdapter.createMapping(domain, containerSpec.name, 8080)

        logger.info { "Deployment complete: service=$serviceId domain=$domain" }
    }

    private fun cloneRepository(
        url: String,
        branch: String,
        serviceId: UUID,
    ): File {
        val repoDir = File(workDir, "service-$serviceId-${System.currentTimeMillis()}")

        try {
            Git
                .cloneRepository()
                .setURI(url)
                .setBranch(branch)
                .setDirectory(repoDir)
                .call()

            logger.info { "Cloned repository: $url (branch: $branch)" }

            return repoDir
        } catch (e: Exception) {
            logger.error(e) { "Failed to clone repository: $url" }

            throw e
        }
    }

    private fun getLatestCommitInfo(repoDir: File): CommitInfo {
        Git.open(repoDir).use { git ->
            val commit =
                git
                    .log()
                    .setMaxCount(1)
                    .call()
                    .first()

            return CommitInfo(
                sha = commit.name,
                message = commit.shortMessage,
                author = commit.authorIdent.name,
            )
        }
    }

    private fun findOrCreateDockerfile(
        repoDir: File,
        appResource: AppResourceEntity,
    ): File {
        val rootDir =
            if (appResource.rootDirectory.isNullOrBlank()) {
                repoDir
            } else {
                File(repoDir, appResource.rootDirectory!!)
            }

        val existingDockerfile = File(rootDir, "Dockerfile")

        if (existingDockerfile.exists()) {
            logger.info { "Using existing Dockerfile" }

            return existingDockerfile
        }

        logger.info { "Generating Dockerfile based on framework detection " }
        val detectedFramework = appResource.framework ?: detectFramework(rootDir)

        return generateDockerfile(rootDir, detectedFramework, appResource)
    }

    private fun detectFramework(dir: File): AppFramework =
        when {
            File(dir, "package.json").exists() -> AppFramework.NODE_JS
            File(dir, "pom.xml").exists() ||
                File(dir, "build.gradle").exists() ||
                File(
                    dir,
                    "build.gradle.kts",
                ).exists() -> AppFramework.SPRING_BOOT

            File(dir, "requirements.txt").exists() -> AppFramework.PYTHON
            File(dir, "go.mod").exists() -> AppFramework.GO
            else -> AppFramework.OTHER
        }

    private fun generateDockerfile(
        rootDir: File,
        framework: AppFramework,
        appResource: AppResourceEntity,
    ): File {
        val dockerfileContent =
            when (framework) {
                AppFramework.SPRING_BOOT ->
                    dockerfileGenerator.generateSpringBootDockerfile(
                        appResource.buildCommand,
                        appResource.startCommand,
                    )

                AppFramework.NODE_JS ->
                    dockerfileGenerator.generateNodeJsDockerfile(
                        appResource.installCommand,
                        appResource.buildCommand,
                        appResource.startCommand,
                    )

                AppFramework.PYTHON ->
                    dockerfileGenerator.generatePythonDockerfile(
                        appResource.installCommand,
                        appResource.startCommand,
                    )

                AppFramework.GO ->
                    dockerfileGenerator.generateGoDockerfile(
                        appResource.buildCommand,
                        appResource.startCommand,
                    )

                else -> dockerfileGenerator.generateGenericDockerfile(appResource.startCommand)
            }

        val dockerfile = File(rootDir, "Dockerfile")

        dockerfile.writeText(dockerfileContent)
        logger.info { "Generated Dockerfile for framework: $framework" }

        return dockerfile
    }

    private fun parseEnvVariables(json: String?): Map<String, String> {
        if (json.isNullOrBlank()) return emptyMap()

        return try {
            json
                .split("\n")
                .filter { it.contains("=") }
                .associate {
                    val (key, value) = it.split("=", limit = 2)
                    key.trim() to value.trim()
                }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to parse environment variables" }

            emptyMap()
        }
    }

    private data class CommitInfo(
        val sha: String,
        val message: String,
        val author: String,
    )
}
