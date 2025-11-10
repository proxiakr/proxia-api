package kr.proxia.domain.service.presentation.request

import kr.proxia.domain.resource.presentation.request.CreateAppResourceRequest
import kr.proxia.domain.resource.presentation.request.CreateDatabaseResourceRequest
import kr.proxia.domain.service.domain.enums.ServiceType

data class UpdateServiceRequest(
    val name: String,
    val description: String?,
    val type: ServiceType,
    val appResource: CreateAppResourceRequest?,
    val databaseResource: CreateDatabaseResourceRequest?,
)
