package kr.proxia.client.docker

import com.github.dockerjava.api.DockerClient as DockerJavaClient
import org.springframework.stereotype.Component

@Component
class DockerClient internal constructor(
    private val dockerJavaClient: DockerJavaClient,
) {
    fun ping(): Boolean =
        runCatching {
            dockerJavaClient.pingCmd().exec()
            true
        }.getOrDefault(false)
}
