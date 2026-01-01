package kr.proxia.domain.resource.domain.error

import kr.proxia.global.error.DomainError
import org.springframework.http.HttpStatus

sealed class ResourceError(
    override val status: HttpStatus,
    override val message: String,
) : DomainError {
    data object NotFound : ResourceError(
        HttpStatus.NOT_FOUND,
        "Resource not found",
    )
}
