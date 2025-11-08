package kr.proxia.global.error

import java.time.LocalDateTime

data class ErrorResponse(
    val code: String,
    val status: Int,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun of(error: BaseError, vararg args: Any): ErrorResponse {
            val formattedMessage = if (args.isNotEmpty()) {
                String.format(error.message, *args)
            } else {
                error.message
            }

            return ErrorResponse(
                code = (error as Enum<*>).name,
                status = error.status.value(),
                message = formattedMessage,
            )
        }
    }
}
