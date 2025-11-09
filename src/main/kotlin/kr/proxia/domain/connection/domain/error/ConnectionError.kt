package kr.proxia.domain.connection.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class ConnectionError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Connection not found"),
    CONNECTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Connection does not belong to user"),
    CONNECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "Connection already exists between these services"),
    INVALID_CONNECTION(HttpStatus.BAD_REQUEST, "Cannot connect service to itself"),
    SOURCE_SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "Source service not found"),
    TARGET_SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "Target service not found"),
}
