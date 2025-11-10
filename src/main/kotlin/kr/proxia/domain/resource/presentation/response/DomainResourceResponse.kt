package kr.proxia.domain.resource.presentation.response

import kr.proxia.domain.resource.domain.entity.DomainResourceEntity

data class DomainResourceResponse(
    val subdomain: String?,
    val customDomain: String?,
) {
    companion object {
        fun of(domainResource: DomainResourceEntity) =
            DomainResourceResponse(
                subdomain = domainResource.subdomain,
                customDomain = domainResource.customDomain,
            )
    }
}
