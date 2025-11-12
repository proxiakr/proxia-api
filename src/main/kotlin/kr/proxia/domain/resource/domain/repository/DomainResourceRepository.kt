package kr.proxia.domain.resource.domain.repository

import kr.proxia.domain.resource.domain.entity.DomainResourceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DomainResourceRepository : JpaRepository<DomainResourceEntity, Long> {
    fun findBySubdomainAndDeletedAtIsNull(subdomain: String): DomainResourceEntity?

    fun findByCustomDomainAndDeletedAtIsNull(customDomain: String): DomainResourceEntity?
}
