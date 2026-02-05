package kr.proxia.core.api.controller.v1.response

import java.time.LocalDateTime
import java.util.UUID

data class WorkspaceDetailResponse(
    val id: UUID,
    val name: String,
    val members: List<WorkspaceMemberResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
