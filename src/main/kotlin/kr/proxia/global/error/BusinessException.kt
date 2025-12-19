package kr.proxia.global.error

open class BusinessException(
    val error: DomainError,
    vararg val args: Any,
) : RuntimeException(
        if (args.isNotEmpty()) String.format(error.message, *args) else error.message,
    )
