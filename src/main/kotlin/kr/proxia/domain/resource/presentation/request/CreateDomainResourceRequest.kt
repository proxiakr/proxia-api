package kr.proxia.domain.resource.presentation.request

data class CreateDomainResourceRequest(
    val domain: String,
    val sslEnabled: Boolean,
)
