package kr.proxia.global.response

import org.springframework.data.domain.Page

fun <T> Page<T>.toResponse() = PageResponse(this.content, hasNext())