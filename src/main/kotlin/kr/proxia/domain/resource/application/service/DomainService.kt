package kr.proxia.domain.resource.application.service

import kr.proxia.domain.resource.domain.entity.DomainResourceEntity
import kr.proxia.domain.resource.domain.repository.DomainResourceRepository
import kr.proxia.global.nginx.properties.NginxProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import javax.naming.directory.InitialDirContext

@Service
class DomainService(
    private val domainResourceRepository: DomainResourceRepository,
    private val nginxProperties: NginxProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateSubdomain(slug: String): String {
        val baseDomain = nginxProperties.baseDomain
        var subdomain = "$slug.$baseDomain"
        var counter = 1

        while (isSubdomainTaken(subdomain)) {
            subdomain = "$slug-$counter.$baseDomain"
            counter++
        }

        return subdomain
    }

    fun isSubdomainTaken(subdomain: String): Boolean = domainResourceRepository.findBySubdomainAndDeletedAtIsNull(subdomain) != null

    fun isCustomDomainTaken(customDomain: String): Boolean =
        domainResourceRepository.findByCustomDomainAndDeletedAtIsNull(customDomain) != null

    fun generateVerificationToken(): String = UUID.randomUUID().toString()

    @Transactional
    fun verifyCustomDomain(
        domainResourceId: Long,
        verificationToken: String,
    ): Boolean {
        val domainResource =
            domainResourceRepository.findById(domainResourceId).orElse(null)
                ?: return false

        val customDomain = domainResource.customDomain ?: return false

        val isVerified = checkDnsTxtRecord(customDomain, verificationToken)

        if (isVerified) {
            domainResource.verify()
            domainResourceRepository.save(domainResource)
            logger.info("Custom domain verified: $customDomain")
        }

        return isVerified
    }

    private fun checkDnsTxtRecord(
        domain: String,
        expectedToken: String,
    ): Boolean {
        try {
            val env = java.util.Hashtable<String, String>()
            env["java.naming.factory.initial"] = "com.sun.jndi.dns.DnsContextFactory"

            val ctx = InitialDirContext(env)
            val txtRecordDomain = "_proxia-verification.$domain"
            val attributes = ctx.getAttributes(txtRecordDomain, arrayOf("TXT"))
            val txtAttribute = attributes.get("TXT")

            if (txtAttribute != null) {
                for (i in 0 until txtAttribute.size()) {
                    val record = txtAttribute.get(i).toString().trim('"')
                    if (record == expectedToken) {
                        return true
                    }
                }
            }

            return false
        } catch (e: Exception) {
            logger.warn("Failed to verify DNS TXT record for domain: $domain", e)
            return false
        }
    }

    fun getFullDomain(domainResource: DomainResourceEntity?): String? {
        if (domainResource == null) return null

        return if (domainResource.customDomain != null && domainResource.verified) {
            domainResource.customDomain
        } else {
            domainResource.subdomain
        }
    }
}
