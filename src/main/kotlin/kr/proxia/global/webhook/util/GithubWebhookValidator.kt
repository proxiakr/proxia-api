package kr.proxia.global.webhook.util

import io.github.oshai.kotlinlogging.KotlinLogging
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger {}

object GithubWebhookValidator {
    fun validateSignature(
        payload: String,
        signature: String,
        secret: String,
    ): Boolean {
        if (secret.isBlank()) {
            logger.warn { "GitHub webhook secret is not configured, skipping signature validation" }
            return true
        }

        try {
            val expectedSignature = calculateSignature(payload, secret)
            return secureCompare(signature, expectedSignature)
        } catch (e: Exception) {
            logger.error(e) { "Failed to validate webhook signature" }
            return false
        }
    }

    private fun calculateSignature(
        payload: String,
        secret: String,
    ): String {
        val hmacSha256 = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        hmacSha256.init(secretKey)

        val hash = hmacSha256.doFinal(payload.toByteArray())
        return "sha256=" + hash.joinToString("") { "%02x".format(it) }
    }

    private fun secureCompare(
        a: String,
        b: String,
    ): Boolean {
        if (a.length != b.length) {
            return false
        }

        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }

        return result == 0
    }
}
