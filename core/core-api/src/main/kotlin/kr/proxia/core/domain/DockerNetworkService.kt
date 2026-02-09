package kr.proxia.core.domain

import kr.proxia.client.docker.DockerClient
import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.support.logging.logger
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DockerNetworkService(
    private val dockerClient: DockerClient,
) {
    private val log = logger()

    fun ensureNetworkExists(projectId: UUID) {
        val name = DockerNaming.network(projectId)
        if (dockerClient.findNetworkByName(name) != null) {
            return
        }
        try {
            val networkId = dockerClient.createNetwork(name)
            log.info { "Created Docker network: $name ($networkId)" }
        } catch (e: Exception) {
            log.error(e) { "Failed to create Docker network: $name" }
            throw CoreException(ErrorType.DOCKER_NETWORK_FAILED)
        }
    }

    fun removeNetwork(projectId: UUID) {
        val name = DockerNaming.network(projectId)
        val existing = dockerClient.findNetworkByName(name) ?: return
        try {
            dockerClient.removeNetwork(existing.id)
            log.info { "Removed Docker network: $name" }
        } catch (e: Exception) {
            log.error(e) { "Failed to remove Docker network: $name" }
            throw CoreException(ErrorType.DOCKER_NETWORK_FAILED)
        }
    }
}
