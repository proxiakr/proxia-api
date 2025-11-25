package kr.proxia.domain.webhook.application.event

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.proxia.domain.deployment.domain.entity.DeploymentEntity
import kr.proxia.domain.deployment.domain.enums.DeploymentStatus
import kr.proxia.domain.deployment.domain.repository.DeploymentRepository
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.domain.repository.GitRepositoryRepository
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
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

        val gitRepository = gitRepositoryRepository.findByIdOrNull(event.gitRepositoryId)
        if (gitRepository == null) {
            logger.error { "GitRepository ${event.gitRepositoryId} not found" }
            return
        }

        val gitIntegration = gitIntegrationRepository.findByIdOrNull(gitRepository.gitIntegrationId)
        if (gitIntegration == null) {
            logger.error { "GitIntegration ${gitRepository.gitIntegrationId} not found" }
            return
        }

        val accessToken = gitIntegration.accessToken

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

        logger.info { "Deployment ${deployment.id} created" }
    }
}
