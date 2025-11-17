package kr.proxia.global.container.docker.factory

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class DockerClientFactory {
    private val clients = ConcurrentHashMap<String, DockerClient>()

    fun getClient(endpoint: String): DockerClient =
        clients.computeIfAbsent(endpoint) {
            createClient(endpoint)
        }

    private fun createClient(endpoint: String): DockerClient {
        val config =
            DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(endpoint)
                .withDockerTlsVerify(false)
                .build()

        val httpClient =
            ApacheDockerHttpClient
                .Builder()
                .dockerHost(config.dockerHost)
                .sslConfig(config.sslConfig)
                .maxConnections(20)
                .connectionTimeout(Duration.ofSeconds(20))
                .responseTimeout(Duration.ofSeconds(30))
                .build()

        return DockerClientImpl.getInstance(config, httpClient)
    }
}
