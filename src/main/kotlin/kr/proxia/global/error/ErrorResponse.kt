package kr.proxia.global.error

import java.time.LocalDateTime

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    constructor(error: DomainError) : this(
        code = error.code,
        message = error.message,
    )
}
