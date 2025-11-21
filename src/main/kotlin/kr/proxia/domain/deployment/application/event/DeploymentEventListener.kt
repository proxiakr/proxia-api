package kr.proxia.domain.deployment.application.event

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.proxia.domain.deployment.domain.entity.DeploymentEntity
import kr.proxia.domain.deployment.domain.enums.DeploymentStatus
import kr.proxia.domain.deployment.domain.repository.DeploymentRepository
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Component
class DeploymentEventListener(
    private val deploymentRepository: DeploymentRepository,
) {
    @Async
    @EventListener
    @Transactional
    fun handle(event: DeploymentEvent) {
        logger.info { "Deploying service ${event.serviceId}" }

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

        // TODO: Build and deploy
    }
}
