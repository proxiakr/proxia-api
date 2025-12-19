package kr.proxia.domain.user.domain.error

import kr.proxia.global.error.DomainError
import org.springframework.http.HttpStatus

sealed class UserError(
    override val status: HttpStatus,
    override val message: String,
) : DomainError {
    data object NotFound : UserError(
        HttpStatus.NOT_FOUND,
        "User not found"
    )
    data object EmailAlreadyExists : UserError(
        HttpStatus.CONFLICT,
        "Email already exists"
    )
    data class InvalidOAuthProvider(val provider: String) : UserError(
        HttpStatus.BAD_REQUEST,
        "User is registered with $provider provider"
    )
}
