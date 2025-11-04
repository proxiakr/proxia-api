package kr.proxia.domain.git.domain.repository

import kr.proxia.domain.git.domain.entity.GitIntegrationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GitIntegrationRepository : JpaRepository<GitIntegrationEntity, Long> {
}