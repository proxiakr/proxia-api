package kr.proxia.domain.webhook.domain.error

import kr.proxia.global.error.DomainError
import org.springframework.http.HttpStatus

sealed class WebhookError(
    override val status: HttpStatus,
    override val message: String,
) : DomainError {
    data object InvalidSignature : WebhookError(
        HttpStatus.UNAUTHORIZED,
        "Invalid webhook signature",
    )
}
