package kr.proxia.global.nginx.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "reverse-proxy.nginx")
data class NginxProperties(
    val enabled: Boolean = true,
    val configDir: String = "/etc/nginx/conf.d",
    val containerName: String = "proxia-nginx",
    val baseDomain: String = "proxia.kr",
    val ssl: SslProperties = SslProperties(),
) {
    data class SslProperties(
        val enabled: Boolean = false,
        val certDir: String = "/etc/letsencrypt/live",
    )
}
