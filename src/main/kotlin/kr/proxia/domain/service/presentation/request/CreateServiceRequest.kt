package kr.proxia.domain.service.presentation.request

import kr.proxia.domain.service.domain.enums.ServiceType

data class CreateServiceRequest(
    val name: String,
    val description: String?,
    val type: ServiceType,
    val x: Double,
    val y: Double,
)
