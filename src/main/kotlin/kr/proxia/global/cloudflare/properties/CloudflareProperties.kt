package kr.proxia.global.cloudflare.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "reverse-proxy.cloudflare")
data class CloudflareProperties(
    val enabled: Boolean = false,
    val accountId: String? = null,
    val apiToken: String? = null,
    val tunnel: TunnelProperties = TunnelProperties(),
) {
    data class TunnelProperties(
        val id: String? = null,
        val name: String = "proxia-tunnel",
        val configPath: String = "/etc/cloudflared",
    )
}
