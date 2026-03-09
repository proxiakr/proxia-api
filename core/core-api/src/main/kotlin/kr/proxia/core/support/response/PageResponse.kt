package kr.proxia.core.support.response

data class PageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
