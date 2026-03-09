package kr.proxia.core.support

data class Page<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
