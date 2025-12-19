package kr.proxia.global.support

data class PageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
