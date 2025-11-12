package kr.proxia.global.docker.config

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import kr.proxia.global.docker.properties.DockerProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI
import java.time.Duration

@Configuration
@EnableConfigurationProperties(DockerProperties::class)
class DockerConfig(
    private val dockerProperties: DockerProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun dockerClient(): DockerClient {
        logger.info("Configuring Docker client with host: ${dockerProperties.host}")

        val dockerHost = URI.create(dockerProperties.host)
        logger.info("Docker host URI: $dockerHost")

        val config =
            DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost.toString())
                .withDockerTlsVerify(false)
                .apply {
                    dockerProperties.registry.url?.let { withRegistryUrl(it) }
                    dockerProperties.registry.username?.let { withRegistryUsername(it) }
                    dockerProperties.registry.password?.let { withRegistryPassword(it) }
                }.build()

        logger.info("Docker client final config - host: ${config.dockerHost}")

        val httpClient =
            ZerodepDockerHttpClient
                .Builder()
                .dockerHost(dockerHost)
                .sslConfig(config.sslConfig)
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build()

        return DockerClientImpl.getInstance(config, httpClient)
    }
}
