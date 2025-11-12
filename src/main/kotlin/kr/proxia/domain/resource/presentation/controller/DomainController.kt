package kr.proxia.domain.resource.presentation.controller

import kr.proxia.domain.resource.application.service.DomainService
import kr.proxia.domain.resource.domain.repository.DomainResourceRepository
import kr.proxia.domain.resource.presentation.request.VerifyDomainRequest
import kr.proxia.domain.resource.presentation.response.VerificationTokenResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/domains")
class DomainController(
    private val domainService: DomainService,
    private val domainResourceRepository: DomainResourceRepository,
) {
    @GetMapping("/{domainId}/verification-token")
    fun getVerificationToken(
        @PathVariable domainId: Long,
    ): ResponseEntity<VerificationTokenResponse> {
        val domainResource =
            domainResourceRepository.findById(domainId).orElse(null)
                ?: return ResponseEntity.notFound().build()

        val token = domainResource.verificationToken ?: domainService.generateVerificationToken()

        if (domainResource.verificationToken == null) {
            domainResource.update(verificationToken = token)
            domainResourceRepository.save(domainResource)
        }

        return ResponseEntity.ok(
            VerificationTokenResponse(
                token = token,
                txtRecord = "_proxia-verification.${domainResource.customDomain}",
                verified = domainResource.verified,
            ),
        )
    }

    @PostMapping("/{domainId}/verify")
    fun verifyDomain(
        @PathVariable domainId: Long,
        @RequestBody request: VerifyDomainRequest,
    ): ResponseEntity<Map<String, Boolean>> {
        val isVerified = domainService.verifyCustomDomain(domainId, request.token)
        return ResponseEntity.ok(mapOf("verified" to isVerified))
    }
}
