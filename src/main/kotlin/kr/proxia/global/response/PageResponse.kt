package kr.proxia.global.response

data class PageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
)