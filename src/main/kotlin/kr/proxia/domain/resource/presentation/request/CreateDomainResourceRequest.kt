package kr.proxia.domain.resource.presentation.request

data class CreateDomainResourceRequest(
    val subdomain: String?,
    val customDomain: String?,
)
