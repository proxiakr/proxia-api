package kr.proxia.domain.webhook.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.proxia.domain.deployment.application.event.DeploymentEvent
import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.domain.webhook.domain.error.WebhookError
import kr.proxia.domain.webhook.infra.properties.WebhookProperties
import kr.proxia.domain.webhook.presentation.request.GithubWebhookPayload
import kr.proxia.global.error.BusinessException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger {}

@Service
class WebhookService(
    private val serviceRepository: ServiceRepository,
    private val appResourceRepository: AppResourceRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val webhookProperties: WebhookProperties,
) {
    fun handleGithub(
        event: String,
        signature: String?,
        raw: String,
        payload: GithubWebhookPayload,
    ) {
        if (!verify(raw, signature)) {
            throw BusinessException(WebhookError.INVALID_SIGNATURE)
        }

        when (event) {
            "push" -> handlePush(payload)
            "ping" -> logger.info { "Github ping" }
            else -> logger.info { "Ignored event: $event" }
        }
    }

    private fun verify(
        payload: String,
        signature: String?,
    ): Boolean {
        val secret = webhookProperties.github.secret
        if (secret.isBlank()) return true

        val expected = "sha256=" + hmacSha256(payload, secret)
        return signature == expected
    }

    private fun hmacSha256(
        data: String,
        secret: String,
    ): String =
        Mac
            .getInstance("HmacSHA256")
            .apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
            .doFinal(data.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun handlePush(payload: GithubWebhookPayload) {
        serviceRepository
            .findAllByTargetIdIsNotNullAndDeletedAtIsNull()
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
