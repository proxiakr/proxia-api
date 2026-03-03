package kr.proxia.core.api.controller.v1.response

import java.util.UUID

data class ServiceDetailResponse(
    val id: UUID,
    val name: String,
    val x: Double,
    val y: Double,
    val status: String,
)
