package kr.proxia.domain.connection.domain.error

import kr.proxia.global.error.DomainError
import org.springframework.http.HttpStatus

sealed class ConnectionError(
    override val status: HttpStatus,
    override val message: String,
) : DomainError {
    data object NotFound : ConnectionError(
        HttpStatus.NOT_FOUND,
        "Connection not found"
    )
    data object AlreadyExists : ConnectionError(
        HttpStatus.CONFLICT,
        "Connection already exists between these services"
    )
    data object SelfConnection : ConnectionError(
        HttpStatus.BAD_REQUEST,
        "Cannot connect service to itself"
    )
    data object SourceNotFound : ConnectionError(
        HttpStatus.NOT_FOUND,
        "Source service not found"
    )
    data object TargetNotFound : ConnectionError(
        HttpStatus.NOT_FOUND,
        "Target service not found"
    )
}
