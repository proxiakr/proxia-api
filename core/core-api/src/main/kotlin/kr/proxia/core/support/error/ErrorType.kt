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

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token expired"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),

    OAUTH_FAILED(HttpStatus.BAD_REQUEST, "OAuth authentication failed"),
    EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "No verified email found"),
    NAME_NOT_FOUND(HttpStatus.BAD_REQUEST, "No name found in OAuth profile"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    WORKSPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "Workspace not found"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Project not found"),
}
