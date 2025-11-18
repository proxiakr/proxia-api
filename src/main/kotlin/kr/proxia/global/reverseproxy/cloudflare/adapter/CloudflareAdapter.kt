package kr.proxia.global.reverseproxy.cloudflare.adapter

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.proxia.global.reverseproxy.ReverseProxyAdapter
import kr.proxia.global.reverseproxy.cloudflare.properties.CloudflareProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.File

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(
    prefix = "reverse-proxy",
    name = ["type"],
    havingValue = "cloudflare_tunnel",
)
class CloudflareAdapter(
    private val cloudflareProperties: CloudflareProperties,
    private val webClient: WebClient,
) : ReverseProxyAdapter {
    private val cfApiBase = "https://api.cloudflare.com/client/v4"

    override val isEnabled: Boolean
        get() = cloudflareProperties.enabled

    override fun createMapping(
        domain: String,
        nodeEndpoint: String,
        hostPort: Int,
    ) {
        if (!isEnabled) {
            logger.info { "Cloudflare disabled: skip mapping" }
            return
        }

        val tunnelId =
            cloudflareProperties.tunnel.id
                ?: error("Cloudflare tunnel ID is missing")

        val apiToken =
            cloudflareProperties.apiToken
                ?: error("Cloudflare API token is missing")

        try {
            val configPath = File(cloudflareProperties.tunnel.configPath, "config.yml")
            updateTunnelConfig(configPath, domain, nodeEndpoint, hostPort)

            createDnsRecord(
                domain = domain,
                tunnelId = tunnelId,
                apiToken = apiToken,
                zoneId = cloudflareProperties.zoneId,
            )

            logger.info { "Cloudflare mapping created: $domain: http://$nodeEndpoint:$hostPort" }
        } catch (e: Exception) {
            logger.error(e) { "Failed creating mapping for $domain" }

            throw e
        }
    }

    override fun deleteMapping(domain: String) {
        if (!isEnabled) return

        try {
            val configPath = File(cloudflareProperties.tunnel.configPath, "config.yml")
            removeTunnelConfig(configPath, domain)

            logger.info { "Cloudflare mapping deleted: $domain" }
        } catch (e: Exception) {
            logger.error(e) { "Failed deleting mapping for $domain" }
        }
    }

    private fun updateTunnelConfig(
        configFile: File,
        domain: String,
        nodeEndpoint: String,
        hostPort: Int,
    ) {
        val existing = readFile(configFile)

        val newIngress =
            """
            - hostname: $domain
              service: http://$nodeEndpoint:$hostPort
            """.trimIndent()

        val updatedYaml =
            if (existing.contains("ingress:")) {
                val lines = existing.lines().toMutableList()
                val catchAllIndex = lines.indexOfFirst { it.contains("http_status:404") }

                if (catchAllIndex != -1) {
                    lines.add(catchAllIndex, newIngress)
                } else {
                    lines.add(newIngress)
                    lines.add("  - service: http_status:404")
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
        configFile.writeText(updatedYaml)
    }

    private fun removeTunnelConfig(
        configFile: File,
        domain: String,
    ) {
        if (!configFile.exists()) return

        val lines = configFile.readLines().toMutableList()
        val index = lines.indexOfFirst { it.contains("hostname: $domain") }

        if (index != -1) {
            lines.removeAt(index)
            if (index < lines.size && lines[index].contains("service:")) {
                lines.removeAt(index)
            }
        }

        configFile.writeText(lines.joinToString("\n"))
    }

    private fun readFile(file: File): String = if (file.exists()) file.readText() else ""

    private fun createDnsRecord(
        domain: String,
        tunnelId: String,
        apiToken: String,
        zoneId: String,
    ) {
        val body =
            mapOf(
                "type" to "CNAME",
                "name" to domain,
                "content" to "$tunnelId.cfargotunnel.com",
                "ttl" to 1,
                "proxied" to true,
            )

        try {
            webClient
                .post()
                .uri("$cfApiBase/zones/$zoneId/dns_records")
                .header("Authorization", "Bearer $apiToken")
                .bodyValue(body)
                .retrieve()
                .bodyToMono<String>()
                .block()

            logger.info { "DNS record created: $domain (CNAME → tunnel)" }
        } catch (e: Exception) {
            logger.warn(e) { "DNS record exists or failed for $domain (ignored)" }
        }
    }
}
