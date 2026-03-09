package kr.proxia.core.api.controller.v1.response

import java.time.LocalDateTime
import java.util.UUID

data class ServiceResponse(
    val id: UUID,
    val name: String,
    val x: Double,
    val y: Double,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
