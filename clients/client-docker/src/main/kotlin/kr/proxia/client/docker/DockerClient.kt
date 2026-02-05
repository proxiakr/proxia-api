package kr.proxia.client.docker

import org.springframework.stereotype.Component
import com.github.dockerjava.api.DockerClient as DockerJavaClient

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
