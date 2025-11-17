package kr.proxia.global.reverseproxy.cloudflare.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "reverse-proxy.cloudflare")
data class CloudflareProperties(
    val enabled: Boolean = false,
    val apiToken: String? = null,
    val baseDomain: String = "proxia.kr",
    val zoneId: String,
    val tunnel: TunnelProperties = TunnelProperties(),
) {
    data class TunnelProperties(
        val id: String? = null,
        val name: String = "proxia-tunnel",
        val configPath: String = "/etc/cloudflared",
        val credentialsFile: String = "/etc/cloudflared/credentials.json",
    )
}
