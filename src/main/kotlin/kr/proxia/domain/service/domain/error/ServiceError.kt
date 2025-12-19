package kr.proxia.domain.service.domain.error

import kr.proxia.global.error.DomainError
import org.springframework.http.HttpStatus

sealed class ServiceError(
    override val status: HttpStatus,
    override val message: String,
) : DomainError {
    data object NotFound : ServiceError(
        HttpStatus.NOT_FOUND,
        "Service not found",
    )

    data object AccessDenied : ServiceError(
        HttpStatus.FORBIDDEN,
        "Service does not belong to user",
    )
}
