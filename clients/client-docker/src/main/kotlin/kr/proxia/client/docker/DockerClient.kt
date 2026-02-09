package kr.proxia.client.docker

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PullResponseItem
import com.github.dockerjava.api.model.RestartPolicy
import com.github.dockerjava.api.model.Volume
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

    fun pullImage(
        image: String,
        tag: String,
    ) {
        dockerJavaClient
            .pullImageCmd(image)
            .withTag(tag)
            .exec(ResultCallback.Adapter<PullResponseItem>())
            .awaitCompletion()
    }

    fun createContainer(
        image: String,
        name: String,
        env: List<String>,
        networkName: String,
        volumeBinding: VolumeBinding? = null,
        cmd: List<String> = emptyList(),
    ): String {
        val hostConfig =
            HostConfig
                .newHostConfig()
                .withNetworkMode(networkName)
                .withRestartPolicy(RestartPolicy.unlessStoppedRestart())

        volumeBinding?.let {
            hostConfig.withBinds(Bind(it.volumeName, Volume(it.containerPath)))
        }

        val createCmd =
            dockerJavaClient
                .createContainerCmd(image)
                .withName(name)
                .withEnv(env)
                .withHostConfig(hostConfig)

        if (cmd.isNotEmpty()) {
            createCmd.withCmd(cmd)
        }

        val response = createCmd.exec()
        return response.id
    }

    fun startContainer(containerId: String) {
        dockerJavaClient.startContainerCmd(containerId).exec()
    }

    fun stopContainer(containerId: String) {
        dockerJavaClient.stopContainerCmd(containerId).exec()
    }

    fun removeContainer(
        containerId: String,
        force: Boolean = false,
    ) {
        dockerJavaClient
            .removeContainerCmd(containerId)
            .withForce(force)
            .exec()
    }

    fun getContainerLogs(
        containerId: String,
        stdout: Boolean = true,
        stderr: Boolean = true,
        tail: Int? = null,
        callback: ResultCallback<Frame>,
    ) {
        val cmd =
            dockerJavaClient
                .logContainerCmd(containerId)
                .withStdOut(stdout)
                .withStdErr(stderr)
        tail?.let { cmd.withTail(it) }
        cmd.exec(callback)
    }

    fun createNetwork(name: String): String {
        val response =
            dockerJavaClient
                .createNetworkCmd()
                .withName(name)
                .withDriver("bridge")
                .exec()
        return response.id
    }

    fun removeNetwork(networkId: String) {
        dockerJavaClient.removeNetworkCmd(networkId).exec()
    }

    fun findNetworkByName(name: String): NetworkInfo? =
        dockerJavaClient
            .listNetworksCmd()
            .withNameFilter(name)
            .exec()
            .firstOrNull { it.name == name }
            ?.let { NetworkInfo(it.id, it.name) }

    fun createVolume(name: String): String {
        val response =
            dockerJavaClient
                .createVolumeCmd()
                .withName(name)
                .exec()
        return response.name
    }

    fun removeVolume(name: String) {
        dockerJavaClient.removeVolumeCmd(name).exec()
    }
}
