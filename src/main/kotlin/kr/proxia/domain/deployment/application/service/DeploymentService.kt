package kr.proxia.domain.deployment.application.service

import kr.proxia.domain.container.domain.entity.ContainerEntity
import kr.proxia.domain.container.domain.enums.ContainerStatus
import kr.proxia.domain.container.domain.repository.ContainerRepository
import kr.proxia.domain.deployment.domain.entity.DeploymentEntity
import kr.proxia.domain.deployment.domain.enums.DeploymentStatus
import kr.proxia.domain.deployment.domain.repository.DeploymentRepository
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.resource.application.service.DomainService
import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.resource.domain.repository.DomainResourceRepository
import kr.proxia.domain.service.domain.enums.ServiceType
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.global.docker.service.DockerService
import kr.proxia.global.error.BusinessException
import kr.proxia.global.reverseproxy.service.ReverseProxyService
import kr.proxia.global.security.encryption.EncryptionService
import org.eclipse.jgit.api.Git
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.time.LocalDateTime

@Service
class DeploymentService(
    private val deploymentRepository: DeploymentRepository,
    private val serviceRepository: ServiceRepository,
    private val projectRepository: ProjectRepository,
    private val appResourceRepository: AppResourceRepository,
    private val containerRepository: ContainerRepository,
    private val domainResourceRepository: DomainResourceRepository,
    private val dockerService: DockerService,
    private val reverseProxyService: ReverseProxyService,
    private val domainService: DomainService,
    private val encryptionService: EncryptionService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val workDir = File(System.getProperty("java.io.tmpdir"), "proxia-deployments")

    init {
        if (!workDir.exists()) {
            workDir.mkdirs()
        }
        dockerService.createNetwork()
    }

    @Async
    @Transactional
    fun deploy(
        serviceId: Long,
        branch: String? = null,
    ) {
        val service =
            serviceRepository.findByIdAndDeletedAtIsNull(serviceId)
                ?: throw BusinessException(kr.proxia.domain.deployment.domain.error.DeploymentError.SERVICE_NOT_FOUND)

        val appResource =
            service.targetId?.let {
                appResourceRepository.findById(it).orElse(null)
            } ?: throw BusinessException(kr.proxia.domain.deployment.domain.error.DeploymentError.APP_RESOURCE_NOT_FOUND)

        val repositoryUrl =
            appResource.repositoryUrl
                ?: throw BusinessException(kr.proxia.domain.deployment.domain.error.DeploymentError.REPOSITORY_URL_NOT_FOUND)

        val deployBranch = branch ?: appResource.branch ?: "main"

        val deployment =
            deploymentRepository.save(
                DeploymentEntity(
                    projectId = service.projectId,
                    commitSha = "",
                    commitMessage = null,
                    commitAuthor = null,
                    branch = deployBranch,
                    status = DeploymentStatus.QUEUED,
                    startedAt = LocalDateTime.now(),
                    finishedAt = null,
                ),
            )

        try {
            updateDeploymentStatus(deployment, DeploymentStatus.CLONING)
            val repoDir = cloneRepository(repositoryUrl, deployBranch, service.id!!)
            val commitInfo = getLatestCommitInfo(repoDir)

            deployment.update(
                commitSha = commitInfo.sha,
                commitMessage = commitInfo.message,
                commitAuthor = commitInfo.author,
            )
            deploymentRepository.save(deployment)

            updateDeploymentStatus(deployment, DeploymentStatus.BUILDING)
            val imageName = "proxia-service-${service.id}"
            val dockerfile = findOrGenerateDockerfile(repoDir, appResource)
            val imageId = dockerService.buildImage(dockerfile, imageName)

            updateDeploymentStatus(deployment, DeploymentStatus.DEPLOYING)
            val existingContainer = containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId)
            if (existingContainer != null) {
                try {
                    dockerService.stopContainer(existingContainer.containerId!!)
                    dockerService.removeContainer(existingContainer.containerId!!)
                } catch (e: Exception) {
                    logger.warn("Failed to remove existing container", e)
                }
                existingContainer.delete()
            }

            val containerName = "proxia-service-${service.id}-${System.currentTimeMillis()}"
            val internalPort = 8080
            val envVariables = parseEnvVariables(appResource.envVariables)

            val containerId =
                dockerService.createContainer(
                    imageName = "$imageName:latest",
                    containerName = containerName,
                    internalPort = internalPort,
                    hostPort = null,
                    envVariables = envVariables,
                )

            dockerService.startContainer(containerId)
            val assignedPort = dockerService.getAssignedPort(containerId, internalPort)

            containerRepository.save(
                ContainerEntity(
                    serviceId = serviceId,
                    containerId = containerId,
                    imageId = imageId,
                    imageName = "$imageName:latest",
                    status = ContainerStatus.RUNNING,
                    port = assignedPort,
                    internalPort = internalPort,
                ),
            )

            assignDomainIfNeeded(service.id, service.projectId, containerName, internalPort)

            updateDeploymentStatus(deployment, DeploymentStatus.RUNNING)
            logger.info("Deployment successful for service $serviceId")
        } catch (e: Exception) {
            logger.error("Deployment failed for service $serviceId", e)
            updateDeploymentStatus(deployment, DeploymentStatus.FAILED)
            throw e
        }
    }

    private fun cloneRepository(
        url: String,
        branch: String,
        serviceId: Long,
    ): File {
        val repoDir = File(workDir, "service-$serviceId-${System.currentTimeMillis()}")
        try {
            Git
                .cloneRepository()
                .setURI(url)
                .setBranch(branch)
                .setDirectory(repoDir)
                .call()
            logger.info("Cloned repository: $url (branch: $branch)")
            return repoDir
        } catch (e: Exception) {
            logger.error("Failed to clone repository: $url", e)
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

    private fun findOrGenerateDockerfile(
        repoDir: File,
        appResource: kr.proxia.domain.resource.domain.entity.AppResourceEntity,
    ): File {
        val rootDir =
            if (appResource.rootDirectory.isNullOrBlank()) {
                repoDir
            } else {
                File(repoDir, appResource.rootDirectory)
            }

        val existingDockerfile = File(rootDir, "Dockerfile")
        if (existingDockerfile.exists()) {
            logger.info("Using existing Dockerfile")
            return existingDockerfile
        }

        logger.info("Generating Dockerfile based on framework detection")
        val detectedFramework = appResource.framework ?: detectFramework(rootDir)
        return generateDockerfile(rootDir, detectedFramework, appResource)
    }

    private fun detectFramework(dir: File): kr.proxia.domain.service.domain.enums.AppFramework =
        when {
            File(dir, "package.json").exists() -> kr.proxia.domain.service.domain.enums.AppFramework.NODE_JS
            File(dir, "pom.xml").exists() || File(dir, "build.gradle").exists() || File(dir, "build.gradle.kts").exists() ->
                kr.proxia.domain.service.domain.enums.AppFramework.SPRING_BOOT
            File(dir, "requirements.txt").exists() -> kr.proxia.domain.service.domain.enums.AppFramework.PYTHON
            File(dir, "go.mod").exists() -> kr.proxia.domain.service.domain.enums.AppFramework.GO
            else -> kr.proxia.domain.service.domain.enums.AppFramework.OTHER
        }

    private fun generateDockerfile(
        rootDir: File,
        framework: kr.proxia.domain.service.domain.enums.AppFramework,
        appResource: kr.proxia.domain.resource.domain.entity.AppResourceEntity,
    ): File {
        val dockerfileContent =
            when (framework) {
                kr.proxia.domain.service.domain.enums.AppFramework.SPRING_BOOT -> generateSpringBootDockerfile(appResource)
                kr.proxia.domain.service.domain.enums.AppFramework.NODE_JS -> generateNodeDockerfile(appResource)
                kr.proxia.domain.service.domain.enums.AppFramework.PYTHON -> generatePythonDockerfile(appResource)
                kr.proxia.domain.service.domain.enums.AppFramework.GO -> generateGoDockerfile(appResource)
                else -> generateGenericDockerfile(appResource)
            }

        val dockerfile = File(rootDir, "Dockerfile")
        dockerfile.writeText(dockerfileContent)
        logger.info("Generated Dockerfile for framework: $framework")
        return dockerfile
    }

    private fun generateSpringBootDockerfile(appResource: kr.proxia.domain.resource.domain.entity.AppResourceEntity): String {
        val buildCommand = appResource.buildCommand ?: "./gradlew build"
        val startCommand = appResource.startCommand ?: "java -jar build/libs/*.jar"

        return """
            FROM eclipse-temurin:21-jdk AS builder
            WORKDIR /app
            COPY . .
            RUN $buildCommand

            FROM eclipse-temurin:21-jre
            WORKDIR /app
            COPY --from=builder /app/build/libs/*.jar app.jar
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }

    private fun generateNodeDockerfile(appResource: kr.proxia.domain.resource.domain.entity.AppResourceEntity): String {
        val installCommand = appResource.installCommand ?: "npm install"
        val buildCommand = appResource.buildCommand ?: "npm run build"
        val startCommand = appResource.startCommand ?: "npm start"

        return """
            FROM node:20-alpine
            WORKDIR /app
            COPY package*.json ./
            RUN $installCommand
            COPY . .
            RUN $buildCommand
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }

    private fun generatePythonDockerfile(appResource: kr.proxia.domain.resource.domain.entity.AppResourceEntity): String {
        val installCommand = appResource.installCommand ?: "pip install -r requirements.txt"
        val startCommand = appResource.startCommand ?: "python app.py"

        return """
            FROM python:3.11-slim
            WORKDIR /app
            COPY requirements.txt .
            RUN $installCommand
            COPY . .
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }

    private fun generateGoDockerfile(appResource: kr.proxia.domain.resource.domain.entity.AppResourceEntity): String {
        val buildCommand = appResource.buildCommand ?: "go build -o app"
        val startCommand = appResource.startCommand ?: "./app"

        return """
            FROM golang:1.21-alpine AS builder
            WORKDIR /app
            COPY . .
            RUN $buildCommand

            FROM alpine:latest
            WORKDIR /app
            COPY --from=builder /app/app .
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }

    private fun generateGenericDockerfile(appResource: kr.proxia.domain.resource.domain.entity.AppResourceEntity): String {
        val startCommand = appResource.startCommand ?: "echo 'No start command provided' && exit 1"

        return """
            FROM ubuntu:22.04
            WORKDIR /app
            COPY . .
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }

    private fun parseEnvVariables(envVariablesJson: String?): Map<String, String> {
        if (envVariablesJson.isNullOrBlank()) return emptyMap()
        return try {
            envVariablesJson
                .split("\n")
                .filter { it.contains("=") }
                .associate {
                    val (key, value) = it.split("=", limit = 2)
                    key.trim() to value.trim()
                }
        } catch (e: Exception) {
            logger.warn("Failed to parse environment variables", e)
            emptyMap()
        }
    }

    private fun assignDomainIfNeeded(
        serviceId: Long,
        projectId: Long,
        containerName: String,
        internalPort: Int,
    ) {
        val service = serviceRepository.findByIdAndDeletedAtIsNull(serviceId) ?: return

        if (service.type == ServiceType.DOMAIN && service.targetId != null) {
            val domainResource = domainResourceRepository.findById(service.targetId!!).orElse(null)
            if (domainResource != null) {
                val domain = domainService.getFullDomain(domainResource)
                if (domain != null) {
                    reverseProxyService.createServiceConfig(domain, containerName, internalPort)
                    logger.info("Assigned domain: $domain to service $serviceId")
                }
            }
        } else {
            val project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
            if (project != null) {
                val subdomain = domainService.generateSubdomain(project.slug)
                val domainResource =
                    domainResourceRepository.save(
                        kr.proxia.domain.resource.domain.entity.DomainResourceEntity(
                            userId = service.userId,
                            subdomain = subdomain,
                            customDomain = null,
                        ),
                    )

                val updatedService = serviceRepository.findByIdAndDeletedAtIsNull(serviceId)
                if (updatedService != null && updatedService.type != ServiceType.DOMAIN) {
                    val newService =
                        kr.proxia.domain.service.domain.entity.ServiceEntity(
                            projectId = projectId,
                            userId = service.userId,
                            name = "${service.name} Domain",
                            description = "Auto-generated domain",
                            type = ServiceType.DOMAIN,
                            x = service.x + 200,
                            y = service.y,
                            targetId = domainResource.id,
                        )
                    serviceRepository.save(newService)
                }

                reverseProxyService.createServiceConfig(subdomain, containerName, internalPort)
                logger.info("Auto-assigned subdomain: $subdomain to service $serviceId")
            }
        }
    }

    private fun updateDeploymentStatus(
        deployment: DeploymentEntity,
        status: DeploymentStatus,
    ) {
        deployment.update(status = status)
        deploymentRepository.save(deployment)
        logger.info("Deployment ${deployment.id} status updated to $status")
    }

    data class CommitInfo(
        val sha: String,
        val message: String,
        val author: String,
    )
}
