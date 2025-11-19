package kr.proxia.domain.webhook.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class WebhookError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "Invalid webhook signature"),
}
