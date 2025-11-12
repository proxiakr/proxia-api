package kr.proxia.domain.project.presentation.response

import java.time.LocalDateTime
import java.util.UUID

data class ProjectDetailResponse(
    val id: UUID,
    val user: User,
    val name: String,
    val slug: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    data class User(
        val id: UUID,
        val name: String,
        val avatarUrl: String?,
    )
}
