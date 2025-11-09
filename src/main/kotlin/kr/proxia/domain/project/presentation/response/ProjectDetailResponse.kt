package kr.proxia.domain.project.presentation.response

import java.time.LocalDateTime

data class ProjectDetailResponse(
    val id: Long,
    val user: User,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    data class User(
        val id: Long,
        val name: String,
        val avatarUrl: String?,
    )
}
