package kr.proxia.global.docker.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "docker")
data class DockerProperties(
    val host: String = "unix:///var/run/docker.sock",
    val tls: TlsProperties = TlsProperties(),
    val network: NetworkProperties = NetworkProperties(),
    val registry: RegistryProperties = RegistryProperties(),
) {
    data class TlsProperties(
        val enabled: Boolean = false,
        val verify: Boolean = true,
        val certPath: String? = null,
        val caCertPath: String? = null,
        val keyPath: String? = null,
    )

    data class NetworkProperties(
        val name: String = "proxia-network",
    )

    data class RegistryProperties(
        val url: String? = null,
        val username: String? = null,
        val password: String? = null,
    )
}
