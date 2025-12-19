package kr.proxia.global.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(e: BusinessException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(e.error.status).body(ErrorResponse(e.error))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                "METHOD_ARGUMENT_NOT_VALID",
                e.bindingResult.allErrors.joinToString(", ") {
                    it.defaultMessage
                        ?: "Invalid value"
                },
            ),
        )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(
                HttpStatus.BAD_REQUEST,
            ).body(ErrorResponse("METHOD_ARGUMENT_TYPE_MISMATCH", "Invalid value for parameter '${e.name}': ${e.value}"))

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(
                HttpStatus.METHOD_NOT_ALLOWED,
            ).body(ErrorResponse("METHOD_NOT_ALLOWED", "HTTP method '${e.method}' is not supported for this endpoint"))

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception occurred", e)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("INTERNAL_SERVER_ERROR", "Internal Server Error"))
    }
}
