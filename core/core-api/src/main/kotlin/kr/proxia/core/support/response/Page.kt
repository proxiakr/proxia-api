package kr.proxia.core.support.response

data class Page<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
