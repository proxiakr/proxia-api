package kr.proxia.domain.project.presentation.response

import java.time.LocalDateTime

data class ProjectResponse(
    val id: Long,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
