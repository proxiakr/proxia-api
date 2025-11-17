package kr.proxia.domain.resource.application.service

import kr.proxia.domain.resource.domain.repository.DomainResourceRepository
import org.springframework.stereotype.Service

@Service
class DomainService(
    private val domainResourceRepository: DomainResourceRepository,
)
