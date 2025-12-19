package kr.proxia.global.support

import org.springframework.data.domain.Page

fun <T> Page<T>.toResponse() = PageResponse(this.content, hasNext())
