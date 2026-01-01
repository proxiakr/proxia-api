package kr.proxia.domain.service.presentation.request

import kr.proxia.domain.resource.presentation.request.CreateAppResourceRequest
import kr.proxia.domain.resource.presentation.request.CreateDatabaseResourceRequest
import kr.proxia.domain.resource.presentation.request.CreateDomainResourceRequest
import kr.proxia.domain.service.domain.enums.ServiceType

data class UpdateServiceRequest(
    val name: String,
    val description: String?,
    val type: ServiceType,
    val appResource: CreateAppResourceRequest?,
    val databaseResource: CreateDatabaseResourceRequest?,
    val domainResource: CreateDomainResourceRequest?,
) {
    init {
        when (type) {
            ServiceType.APP -> requireNotNull(appResource) { "appResource field is required." }
            ServiceType.DATABASE -> requireNotNull(databaseResource) { "databaseResource field is required." }
            ServiceType.DOMAIN -> requireNotNull(domainResource) { "domainResource field is required." }
        }
    }
}
