package kr.proxia.domain.resource.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.domain.resource.domain.enums.SslStatus
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(name = "domain_resources")
class DomainResourceEntity(
    val userId: Long,
    subdomain: String?,
    customDomain: String?,
    verified: Boolean = false,
    verificationToken: String? = null,
    sslStatus: SslStatus = SslStatus.PENDING,
) : BaseEntity() {
    var subdomain: String? = subdomain
        protected set

    var customDomain: String? = customDomain
        protected set

    var verified: Boolean = verified
        protected set

    var verificationToken: String? = verificationToken
        protected set

    @Enumerated(EnumType.STRING)
    var sslStatus: SslStatus = sslStatus
        protected set

    fun update(
        subdomain: String? = this.subdomain,
        customDomain: String? = this.customDomain,
        verified: Boolean = this.verified,
        verificationToken: String? = this.verificationToken,
        sslStatus: SslStatus = this.sslStatus,
    ) {
        this.subdomain = subdomain
        this.customDomain = customDomain
        this.verified = verified
        this.verificationToken = verificationToken
        this.sslStatus = sslStatus
    }

    fun verify() {
        this.verified = true
    }
}
