package kr.proxia.global.cloudflare.service

import kr.proxia.global.cloudflare.properties.CloudflareProperties
import kr.proxia.global.reverseproxy.service.ReverseProxyService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.File

@Service
@ConditionalOnProperty(prefix = "reverse-proxy", name = ["type"], havingValue = "cloudflare_tunnel")
@EnableConfigurationProperties(CloudflareProperties::class)
class CloudflareTunnelService(
    private val cloudflareProperties: CloudflareProperties,
    private val webClient: WebClient,
) : ReverseProxyService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val cfApiBase = "https://api.cloudflare.com/client/v4"

    override fun isEnabled(): Boolean = cloudflareProperties.enabled

    override fun createServiceConfig(
        domain: String,
        containerName: String,
        internalPort: Int,
    ) {
        if (!cloudflareProperties.enabled) {
            logger.info("Cloudflare Tunnel is disabled, skipping config generation")
            return
        }

        val accountId =
            cloudflareProperties.accountId
                ?: throw IllegalStateException("Cloudflare account ID is not configured")
        val tunnelId =
            cloudflareProperties.tunnel.id
                ?: throw IllegalStateException("Cloudflare tunnel ID is not configured")
        val apiToken =
            cloudflareProperties.apiToken
                ?: throw IllegalStateException("Cloudflare API token is not configured")

        try {
            val configFile = File(cloudflareProperties.tunnel.configPath, "config.yml")
            updateCloudflaredConfig(configFile, domain, containerName, internalPort)

            createDnsRecord(domain, accountId, tunnelId, apiToken)

            logger.info("Created Cloudflare Tunnel config for domain: $domain -> $containerName:$internalPort")
        } catch (e: Exception) {
            logger.error("Failed to create Cloudflare Tunnel config for domain: $domain", e)
            throw e
        }
    }

    override fun deleteServiceConfig(domain: String) {
        if (!cloudflareProperties.enabled) {
            return
        }

        try {
            val configFile = File(cloudflareProperties.tunnel.configPath, "config.yml")
            removeFromCloudflaredConfig(configFile, domain)

            logger.info("Deleted Cloudflare Tunnel config for domain: $domain")
        } catch (e: Exception) {
            logger.error("Failed to delete Cloudflare Tunnel config for domain: $domain", e)
        }
    }

    private fun updateCloudflaredConfig(
        configFile: File,
        domain: String,
        containerName: String,
        internalPort: Int,
    ) {
        val config = readOrCreateConfig(configFile)
        val newIngress =
            """
            - hostname: $domain
              service: http://$containerName:$internalPort
            """.trimIndent()

        val updatedConfig =
            if (config.contains("ingress:")) {
                val lines = config.lines().toMutableList()
                val ingressIndex = lines.indexOfFirst { it.trim().startsWith("ingress:") }
                if (ingressIndex != -1) {
                    val catchAllIndex = lines.indexOfFirst { it.contains("service: http_status:404") }
                    if (catchAllIndex != -1) {
                        lines.add(catchAllIndex, newIngress)
                    } else {
                        lines.add(newIngress)
                        lines.add("  - service: http_status:404")
                    }
                }
                lines.joinToString("\n")
            } else {
                """
                tunnel: ${cloudflareProperties.tunnel.id}
                credentials-file: ${cloudflareProperties.tunnel.configPath}/credentials.json

                ingress:
                $newIngress
                  - service: http_status:404
                """.trimIndent()
            }

        configFile.parentFile?.mkdirs()
        configFile.writeText(updatedConfig)
    }

    private fun removeFromCloudflaredConfig(
        configFile: File,
        domain: String,
    ) {
        if (!configFile.exists()) return

        val config = configFile.readText()
        val lines = config.lines().toMutableList()
        val domainLineIndex = lines.indexOfFirst { it.contains("hostname: $domain") }

        if (domainLineIndex != -1) {
            lines.removeAt(domainLineIndex)
            if (domainLineIndex < lines.size && lines[domainLineIndex].contains("service:")) {
                lines.removeAt(domainLineIndex)
            }
        }

        configFile.writeText(lines.joinToString("\n"))
    }

    private fun readOrCreateConfig(configFile: File): String =
        if (configFile.exists()) {
            configFile.readText()
        } else {
            ""
        }

    private fun createDnsRecord(
        domain: String,
        @Suppress("UNUSED_PARAMETER") accountId: String,
        tunnelId: String,
        apiToken: String,
    ) {
        val zoneId = extractZoneFromDomain(domain)

        try {
            val dnsRecord =
                mapOf(
                    "type" to "CNAME",
                    "name" to domain,
                    "content" to "$tunnelId.cfargotunnel.com",
                    "ttl" to 1,
                    "proxied" to true,
                )

            webClient
                .post()
                .uri("$cfApiBase/zones/$zoneId/dns_records")
                .header("Authorization", "Bearer $apiToken")
                .header("Content-Type", "application/json")
                .bodyValue(dnsRecord)
                .retrieve()
                .bodyToMono<String>()
                .block()

            logger.info("Created DNS CNAME record for domain: $domain")
        } catch (e: Exception) {
            logger.warn("Failed to create DNS record (might already exist): $domain", e)
        }
    }

    private fun extractZoneFromDomain(domain: String): String {
        val parts = domain.split(".")
        return if (parts.size >= 2) {
            "${parts[parts.size - 2]}.${parts[parts.size - 1]}"
        } else {
            domain
        }
    }
}
