package kr.proxia.domain.webhook.application.event

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.proxia.domain.deployment.domain.entity.DeploymentEntity
import kr.proxia.domain.deployment.domain.enums.DeploymentStatus
import kr.proxia.domain.deployment.domain.repository.DeploymentRepository
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.domain.repository.GitRepositoryRepository
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Component
class DeploymentEventListener(
    private val deploymentRepository: DeploymentRepository,
    private val gitRepositoryRepository: GitRepositoryRepository,
    private val gitIntegrationRepository: GitIntegrationRepository,
) {
    @Async
    @EventListener
    @Transactional
    fun handle(event: DeploymentEvent) {
        logger.info { "Deploying service ${event.serviceId}" }

        // Query accessToken: GitRepository → GitIntegration → accessToken
        val gitRepository = gitRepositoryRepository.findById(event.gitRepositoryId).orElse(null)
        if (gitRepository == null) {
            logger.error { "GitRepository ${event.gitRepositoryId} not found" }
            return
        }

        val gitIntegration = gitIntegrationRepository.findById(gitRepository.gitIntegrationId).orElse(null)
        if (gitIntegration == null) {
            logger.error { "GitIntegration ${gitRepository.gitIntegrationId} not found" }
            return
        }

        val accessToken = gitIntegration.accessToken
        logger.info { "Retrieved access token for deployment" }

        val deployment =
            deploymentRepository.save(
                DeploymentEntity(
                    projectId = event.projectId,
                    commitSha = event.commitSha,
                    commitMessage = event.commitMessage,
                    commitAuthor = event.commitAuthor,
                    branch = event.branch,
                    status = DeploymentStatus.PENDING,
                    startedAt = null,
                    finishedAt = null,
                ),
            )

        logger.info { "Deployment ${deployment.id} created with access token" }

        // TODO: Build and deploy using accessToken
    }
}
