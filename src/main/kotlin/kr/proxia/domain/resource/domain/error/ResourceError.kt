package kr.proxia.domain.resource.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class ResourceError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
}
