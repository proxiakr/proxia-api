package kr.proxia.core.api.controller.v1.response

import java.time.LocalDateTime
import java.util.UUID

data class ProjectDetailResponse(
    val id: UUID,
    val name: String,
    val subdomain: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
