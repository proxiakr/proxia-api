package kr.proxia.global.docker.config

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import kr.proxia.global.docker.properties.DockerProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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

        val config =
            DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .apply {
                    // Explicitly set docker host - this should override environment variables
                    withDockerHost(dockerProperties.host)

                    if (dockerProperties.tls.enabled) {
                        withDockerTlsVerify(dockerProperties.tls.verify)
                        dockerProperties.tls.certPath?.let { withDockerCertPath(it) }
                    } else {
                        withDockerTlsVerify(false)
                    }
                    dockerProperties.registry.url?.let { withRegistryUrl(it) }
                    dockerProperties.registry.username?.let { withRegistryUsername(it) }
                    dockerProperties.registry.password?.let { withRegistryPassword(it) }
                }.build()

        logger.info("Docker client configured with host: ${config.dockerHost}")

        val httpClient =
            ApacheDockerHttpClient
                .Builder()
                .dockerHost(config.dockerHost)
                .sslConfig(config.sslConfig)
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build()

        return DockerClientImpl.getInstance(config, httpClient)
    }
}
