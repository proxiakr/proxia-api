package kr.proxia.domain.webhook.presentation.controller

import kr.proxia.domain.webhook.application.service.WebhookService
import kr.proxia.domain.webhook.presentation.request.GithubWebhookPayload
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/webhooks")
class WebhookController(
    private val webhookService: WebhookService,
) {
    @PostMapping("/github")
    fun github(
        @RequestHeader("X-GitHub-Event") event: String,
        @RequestHeader("X-Hub-Signature-256", required = false) signature: String?,
        @RequestBody raw: String,
        @RequestBody payload: GithubWebhookPayload,
    ) = webhookService.handleGithub(event, signature, raw, payload)
}
