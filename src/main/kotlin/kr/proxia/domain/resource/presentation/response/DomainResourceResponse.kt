package kr.proxia.domain.resource.presentation.response

import kr.proxia.domain.resource.domain.entity.DomainResourceEntity

data class DomainResourceResponse(
    val domain: String,
    val sslEnabled: Boolean,
) {
    companion object {
        fun of(domainResource: DomainResourceEntity) =
            DomainResourceResponse(
                domain = domainResource.domain,
                sslEnabled = domainResource.sslEnabled,
            )
    }
}
