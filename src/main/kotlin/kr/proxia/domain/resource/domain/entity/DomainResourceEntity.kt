package kr.proxia.domain.resource.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import kr.proxia.global.jpa.common.BaseEntity
import java.util.UUID

@Entity
@Table(name = "domain_resources")
class DomainResourceEntity(
    val userId: UUID,
    subdomain: String?,
    customDomain: String?,
) : BaseEntity() {
    var subdomain: String? = subdomain
        protected set

    var customDomain: String? = customDomain
        protected set

    fun update(
        subdomain: String? = this.subdomain,
        customDomain: String? = this.customDomain,
    ) {
        this.subdomain = subdomain
        this.customDomain = customDomain
    }
}
