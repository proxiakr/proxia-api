package kr.proxia.domain.webhook.application.service

import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.domain.webhook.application.event.DeploymentEvent
import kr.proxia.domain.webhook.presentation.request.GithubWebhookPayload
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WebhookService(
    private val serviceRepository: ServiceRepository,
    private val appResourceRepository: AppResourceRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun handlePush(payload: GithubWebhookPayload) {
        serviceRepository
            .findAll()
            .filter { it.deletedAt == null && it.targetId != null }
            .forEach { service ->
                val resource = appResourceRepository.findByIdOrNull(service.targetId!!) ?: return@forEach

                if (resource.repositoryUrl != payload.repositoryUrl || resource.branch != payload.branch) {
                    return@forEach
                }

                eventPublisher.publishEvent(
                    DeploymentEvent(
                        serviceId = service.id,
                        projectId = service.projectId,
                        gitUrl = payload.repositoryUrl,
                        branch = payload.branch,
                        commitSha = payload.headCommit?.id ?: "",
                        commitMessage = payload.headCommit?.message,
                        commitAuthor = payload.headCommit?.author?.name,
                    ),
                )
            }
    }
}
