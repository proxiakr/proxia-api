package kr.proxia.global.webhook.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "webhook")
data class WebhookProperties(
    val github: GithubWebhookProperties = GithubWebhookProperties(),
) {
    data class GithubWebhookProperties(
        val secret: String? = null,
    )
}
