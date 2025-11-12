package kr.proxia.global.docker.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.BuildImageResultCallback
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Ports
import com.github.dockerjava.api.model.Volume
import kr.proxia.global.docker.properties.DockerProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class DockerService(
    private val dockerClient: DockerClient,
    private val dockerProperties: DockerProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createNetwork() {
        try {
            val networks = dockerClient.listNetworksCmd().exec()
            val networkExists = networks.any { it.name == dockerProperties.network.name }

            if (!networkExists) {
                dockerClient
                    .createNetworkCmd()
                    .withName(dockerProperties.network.name)
                    .withDriver("bridge")
                    .exec()
                logger.info("Created Docker network: ${dockerProperties.network.name}")
            }
        } catch (e: Exception) {
            logger.error("Failed to create network", e)
            throw e
        }
    }

    fun buildImage(
        dockerfile: File,
        imageName: String,
        tags: Set<String> = setOf("latest"),
    ): String {
        try {
            val imageId =
                dockerClient
                    .buildImageCmd()
                    .withDockerfile(dockerfile)
                    .withTags(tags.map { "$imageName:$it" }.toSet())
                    .exec(BuildImageResultCallback())
                    .awaitImageId()

            logger.info("Built image: $imageName with ID: $imageId")
            return imageId
        } catch (e: Exception) {
            logger.error("Failed to build image: $imageName", e)
            throw e
        }
    }

    fun createContainer(
        imageName: String,
        containerName: String,
        internalPort: Int,
        hostPort: Int?,
        envVariables: Map<String, String> = emptyMap(),
        volumes: Map<String, String> = emptyMap(),
    ): String {
        try {
            val exposedPort = ExposedPort.tcp(internalPort)
            val portBindings =
                if (hostPort != null) {
                    Ports().apply {
                        bind(exposedPort, Ports.Binding.bindPort(hostPort))
                    }
                } else {
                    Ports().apply {
                        bind(exposedPort, Ports.Binding.empty())
                    }
                }

            val volumeBinds =
                volumes.map { (hostPath, containerPath) ->
                    Bind(hostPath, Volume(containerPath))
                }

            val hostConfig =
                HostConfig
                    .newHostConfig()
                    .withPortBindings(portBindings)
                    .withBinds(volumeBinds)
                    .withNetworkMode(dockerProperties.network.name)

            val container =
                dockerClient
                    .createContainerCmd(imageName)
                    .withName(containerName)
                    .withExposedPorts(exposedPort)
                    .withHostConfig(hostConfig)
                    .withEnv(envVariables.map { "${it.key}=${it.value}" })
                    .exec()

            logger.info("Created container: $containerName with ID: ${container.id}")
            return container.id
        } catch (e: Exception) {
            logger.error("Failed to create container: $containerName", e)
            throw e
        }
    }

    fun startContainer(containerId: String) {
        try {
            dockerClient.startContainerCmd(containerId).exec()
            logger.info("Started container: $containerId")
        } catch (e: Exception) {
            logger.error("Failed to start container: $containerId", e)
            throw e
        }
    }

    fun stopContainer(containerId: String) {
        try {
            dockerClient.stopContainerCmd(containerId).exec()
            logger.info("Stopped container: $containerId")
        } catch (e: Exception) {
            logger.error("Failed to stop container: $containerId", e)
            throw e
        }
    }

    fun removeContainer(containerId: String) {
        try {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec()
            logger.info("Removed container: $containerId")
        } catch (e: Exception) {
            logger.error("Failed to remove container: $containerId", e)
            throw e
        }
    }

    fun getContainerLogs(
        containerId: String,
        tail: Int = 100,
    ): String {
        try {
            val logBuilder = StringBuilder()
            dockerClient
                .logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTail(tail)
                .exec(
                    object : com.github.dockerjava.api.async.ResultCallback.Adapter<com.github.dockerjava.api.model.Frame>() {
                        override fun onNext(frame: com.github.dockerjava.api.model.Frame) {
                            logBuilder.append(String(frame.payload))
                        }
                    },
                ).awaitCompletion()

            return logBuilder.toString()
        } catch (e: Exception) {
            logger.error("Failed to get logs for container: $containerId", e)
            throw e
        }
    }

    fun isContainerRunning(containerId: String): Boolean {
        try {
            val container = dockerClient.inspectContainerCmd(containerId).exec()
            return container.state.running ?: false
        } catch (e: Exception) {
            logger.error("Failed to check container status: $containerId", e)
            return false
        }
    }

    fun getAssignedPort(
        containerId: String,
        internalPort: Int,
    ): Int? {
        try {
            val container = dockerClient.inspectContainerCmd(containerId).exec()
            val portBindings = container.networkSettings.ports.bindings
            val exposedPort = ExposedPort.tcp(internalPort)
            return portBindings[exposedPort]?.firstOrNull()?.hostPortSpec?.toIntOrNull()
        } catch (e: Exception) {
            logger.error("Failed to get assigned port for container: $containerId", e)
            return null
        }
    }
}
