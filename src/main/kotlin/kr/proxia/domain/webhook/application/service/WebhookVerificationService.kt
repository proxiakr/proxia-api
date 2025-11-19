package kr.proxia.domain.webhook.application.service

import kr.proxia.domain.webhook.properties.WebhookProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
@EnableConfigurationProperties(WebhookProperties::class)
class WebhookVerificationService(
    private val webhookProperties: WebhookProperties,
) {
    fun verify(
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
}
