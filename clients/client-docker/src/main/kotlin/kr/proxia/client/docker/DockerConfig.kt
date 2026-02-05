package kr.proxia.client.docker

import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import com.github.dockerjava.api.DockerClient as DockerJavaClient

@Configuration
internal class DockerConfig {
    @Bean
    fun dockerJavaClient(): DockerJavaClient {
        val config =
            DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build()

        val httpClient =
            ZerodepDockerHttpClient
                .Builder()
                .dockerHost(config.dockerHost)
                .sslConfig(config.sslConfig)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build()

        return DockerClientImpl.getInstance(config, httpClient)
    }
}
