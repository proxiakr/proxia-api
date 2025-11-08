package kr.proxia.global.error

import org.springframework.http.HttpStatus

enum class CommonError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Invalid input value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "Invalid type value"),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
}
