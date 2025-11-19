package kr.proxia.domain.webhook.presentation.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.proxia.domain.webhook.application.service.WebhookService
import kr.proxia.domain.webhook.application.service.WebhookVerificationService
import kr.proxia.domain.webhook.presentation.request.GithubWebhookPayload
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/webhooks")
class WebhookController(
    private val webhookService: WebhookService,
    private val verificationService: WebhookVerificationService,
) {
    @PostMapping("/github")
    fun github(
        @RequestHeader("X-GitHub-Event") event: String,
        @RequestHeader("X-Hub-Signature-256", required = false) signature: String?,
        @RequestBody raw: String,
        @RequestBody payload: GithubWebhookPayload,
    ) {
        if (!verificationService.verify(raw, signature)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid signature")
        }

        when (event) {
            "push" -> webhookService.handlePush(payload)
            "ping" -> logger.info { "Github ping" }
            else -> logger.info { "Ignored event: $event" }
        }
    }
}
