package kr.proxia.domain.service.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class ServiceError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "Service not found"),
    SERVICE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Service does not belong to user"),
    SERVICE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "Service already deleted"),
    SERVICE_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Service name already exists in this project"),
}
