package kr.proxia.global.error

open class BusinessException(
    val error: BaseError,
    vararg val args: Any,
) : RuntimeException(
        if (args.isNotEmpty()) String.format(error.message, *args) else error.message,
    )
