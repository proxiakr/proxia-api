package kr.proxia.domain.project.presentation.v1.response

import java.time.LocalDateTime

data class ProjectResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)