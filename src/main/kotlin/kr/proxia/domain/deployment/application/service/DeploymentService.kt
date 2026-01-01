package kr.proxia.domain.deployment.application.service

import kr.proxia.domain.deployment.domain.entity.DeploymentEntity
import kr.proxia.domain.deployment.domain.enums.DeploymentStatus
import kr.proxia.domain.deployment.domain.error.DeploymentError
import kr.proxia.domain.deployment.domain.repository.DeploymentRepository
import kr.proxia.domain.deployment.infra.builder.ImageBuilder
import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.infra.client.GitRepositoryClient
import kr.proxia.domain.project.domain.error.ProjectError
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.resource.domain.error.ResourceError
import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.service.domain.enums.ServiceType
import kr.proxia.domain.service.domain.error.ServiceError
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.github.properties.GithubProperties
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class DeploymentService(
    private val serviceRepository: ServiceRepository,
    private val appResourceRepository: AppResourceRepository,
    private val deploymentRepository: DeploymentRepository,
    private val gitIntegrationRepository: GitIntegrationRepository,
    private val securityHolder: SecurityHolder,
    private val gitRepositoryClient: GitRepositoryClient,
    private val imageBuilder: ImageBuilder,
    private val githubProperties: GithubProperties,
    private val projectRepository: ProjectRepository,
) {
    @Transactional
    suspend fun deployApp(
        projectId: UUID,
        serviceId: UUID,
    ) {
        val service = serviceRepository.findByIdOrNull(serviceId)
            ?: throw BusinessException(ServiceError.NotFound)

        val userId = securityHolder.getUserId()
        if (service.userId != userId) {
            throw BusinessException(ServiceError.AccessDenied)
        }

        val type = service.type
        if (type != ServiceType.APP) {
            throw BusinessException(DeploymentError.InvalidResourceDeployment(type))
        }

        val appResource = appResourceRepository.findByIdOrNull(service.targetId)
            ?: throw BusinessException(ResourceError.NotFound)

        val integration = gitIntegrationRepository.findByUserIdAndProvider(userId, GitIntegrationProvider.GITHUB)
            ?: throw BusinessException(GitError.NotFound)

        val repository = gitRepositoryClient.getGitRepository(
            accessToken = integration.accessToken,
            url = appResource.repositoryUrl,
            branch = "main",
        )

        val deployment = DeploymentEntity(
            serviceId = serviceId,
            commitSha = repository.commitSha,
            commitMessage = repository.commitMessage,
            commitAuthor = repository.commitAuthor,
            branch = repository.branch,
            status = DeploymentStatus.PENDING,
            startedAt = LocalDateTime.now(),
            finishedAt = null,
        )

        deploymentRepository.save(deployment)

        val project = projectRepository.findByIdOrNull(projectId)
            ?: throw BusinessException(ProjectError.NotFound)

        imageBuilder.build(
            projectName = project.name,
            url = appResource.repositoryUrl,
            targetImage = "ghcr.io/${githubProperties.username}/${service.name}/1.0.0",
            deploymentId = deployment.id.toString(),
            serviceName = service.name,
        )
    }
}
