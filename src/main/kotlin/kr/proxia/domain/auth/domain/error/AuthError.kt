package kr.proxia.domain.auth.domain.error

import kr.proxia.global.error.DomainError
import org.springframework.http.HttpStatus

//enum class AuthError(
//    override val status: HttpStatus,
//    override val message: String,
//) : DomainError {
//    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
//    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Expired token"),
//    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
//    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid password"),
//    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token not found"),
//}

sealed class AuthError(
    override val status: HttpStatus,
    override val message: String,
) : DomainError {
    data object InvalidToken : AuthError(
        HttpStatus.UNAUTHORIZED,
        "Invalid token"
    )
    data object ExpiredToken : AuthError(
        HttpStatus.UNAUTHORIZED,
        "Expired token"
    )
    data object InvalidCredentials : AuthError(
        HttpStatus.UNAUTHORIZED,
        "Invalid credentials"
    )
    data object InvalidPassword : AuthError(
        HttpStatus.UNAUTHORIZED,
        "Invalid password"
    )
    data object RefreshTokenNotFound : AuthError(
        HttpStatus.NOT_FOUND,
        "Refresh token not found"
    )
}