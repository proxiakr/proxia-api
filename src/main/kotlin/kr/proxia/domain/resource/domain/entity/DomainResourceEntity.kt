package kr.proxia.domain.resource.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(name = "domain_resources")
class DomainResourceEntity(
    val userId: Long,
    domain: String,
    sslEnabled: Boolean,
) : BaseEntity() {
    var domain: String = domain
        protected set

    var sslEnabled: Boolean = sslEnabled
        protected set

    fun update(
        domain: String = this.domain,
        sslEnabled: Boolean = this.sslEnabled,
    ) {
        this.domain = domain
        this.sslEnabled = sslEnabled
    }
}
