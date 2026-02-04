package kr.proxia.core.support.error

import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus

enum class ErrorType(
    val status: HttpStatus,
    val message: String,
    val logLevel: LogLevel = LogLevel.WARN,
) {
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", LogLevel.ERROR),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
}
