package kr.proxia.global.response

import org.springframework.data.domain.PageRequest

data class OffsetLimit(
    val offset: Long,
    val limit: Int,
) {
    fun toPageable() = PageRequest.of((offset / limit).toInt(), limit)
}
