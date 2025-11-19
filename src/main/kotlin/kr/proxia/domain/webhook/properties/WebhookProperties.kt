package kr.proxia.domain.webhook.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "webhook")
data class WebhookProperties(
    val github: Github,
) {
    data class Github(
        val secret: String = "",
    )
}
