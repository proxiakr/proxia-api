package kr.proxia.domain.webhook.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kr.proxia.domain.webhook.application.service.WebhookService
import kr.proxia.global.webhook.properties.WebhookProperties
import kr.proxia.global.webhook.util.GithubWebhookValidator
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/webhooks")
@EnableConfigurationProperties(WebhookProperties::class)
class WebhookController(
    private val webhookService: WebhookService,
    private val webhookProperties: WebhookProperties,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/github")
    fun handleGithubWebhook(
        @RequestBody payload: String,
        @RequestHeader("X-Hub-Signature-256", required = false) signature: String?,
        @RequestHeader("X-GitHub-Event", required = false) event: String?,
        @RequestHeader("X-GitHub-Delivery", required = false) deliveryId: String?,
    ): ResponseEntity<Map<String, String>> {
        if (signature == null) {
            logger.warn("Received webhook without signature")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Missing signature"))
        }

        val secret = webhookProperties.github.secret ?: ""
        if (!GithubWebhookValidator.validateSignature(payload, signature, secret)) {
            logger.warn("Invalid webhook signature")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid signature"))
        }

        if (event == null) {
            logger.warn("Received webhook without event type")
            return ResponseEntity.badRequest().body(mapOf("error" to "Missing event type"))
        }

        logger.info("Received GitHub webhook: event=$event, deliveryId=$deliveryId")

        try {
            val payloadMap = objectMapper.readValue(payload, Map::class.java) as Map<String, Any>

            when (event) {
                "push" -> {
                    webhookService.handleGithubPushEvent(event, payloadMap, deliveryId)
                    return ResponseEntity.ok(mapOf("status" to "success", "message" to "Push event processed"))
                }
                "ping" -> {
                    logger.info("Received ping event from GitHub")
                    return ResponseEntity.ok(mapOf("status" to "success", "message" to "Pong"))
                }
                else -> {
                    logger.info("Unhandled event type: $event")
                    return ResponseEntity.ok(mapOf("status" to "ignored", "message" to "Event type not handled"))
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to process webhook", e)
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to process webhook", "message" to (e.message ?: "Unknown error")))
        }
    }
}
