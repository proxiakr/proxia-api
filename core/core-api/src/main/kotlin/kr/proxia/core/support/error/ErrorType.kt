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

    UNSUPPORTED_DATABASE_VERSION(HttpStatus.BAD_REQUEST, "Unsupported database version"),

    DOCKER_IMAGE_PULL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to pull Docker image", LogLevel.ERROR),
    DOCKER_CONTAINER_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create Docker container", LogLevel.ERROR),
    DOCKER_CONTAINER_START_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start Docker container", LogLevel.ERROR),
    DOCKER_CONTAINER_STOP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to stop Docker container", LogLevel.ERROR),
    DOCKER_NETWORK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Docker network operation failed", LogLevel.ERROR),
}
