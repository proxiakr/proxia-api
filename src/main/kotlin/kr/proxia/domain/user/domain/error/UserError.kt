package kr.proxia.domain.user.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class UserError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email already exists"),
    INVALID_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "User is registered with %s provider"),
}
