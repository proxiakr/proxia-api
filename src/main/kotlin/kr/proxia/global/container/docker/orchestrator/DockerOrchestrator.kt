package kr.proxia.global.container.docker.orchestrator

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Ports
import com.github.dockerjava.api.model.Volume
import io.github.oshai.kotlinlogging.KotlinLogging
import kr.proxia.global.container.ContainerOrchestrator
import kr.proxia.global.container.ContainerSpec
import kr.proxia.global.container.docker.factory.DockerClientFactory
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class DockerOrchestrator(
    private val dockerClientFactory: DockerClientFactory,
) : ContainerOrchestrator {
    override fun createContainer(
        endpoint: String,
        spec: ContainerSpec,
    ): String {
        val client = dockerClientFactory.getClient(endpoint)

        val exposedPorts = spec.ports.map { ExposedPort.tcp(it.internal) }

        val portBindings =
            Ports().apply {
                spec.ports.forEach {
                    val exposed = ExposedPort.tcp(it.internal)
                    val binding =
                        it.host?.let { hostPort ->
                            Ports.Binding.bindPort(hostPort)
                        } ?: Ports.Binding.empty()

                    bind(exposed, binding)
                }
            }

        val volumeBinds =
            spec.volumes.map {
                Bind(it.hostPath, Volume(it.containerPath))
            }

        val hostConfig =
            HostConfig
                .newHostConfig()
                .withPortBindings(portBindings)
                .withBinds(volumeBinds)
                .apply {
                    spec.network?.let { withNetworkMode(it) }
                }

        val cmd =
            client
                .createContainerCmd(spec.image)
                .withName(spec.name)
                .withExposedPorts(exposedPorts)
                .withHostConfig(hostConfig)
                .withEnv(spec.env.map { "${it.key}=${it.value}" })

        spec.command?.let { cmd.withCmd(it) }

        val container = cmd.exec()
        return container.id
    }

    override fun startContainer(
        endpoint: String,
        containerId: String,
    ) {
        dockerClientFactory
            .getClient(endpoint)
            .startContainerCmd(containerId)
            .exec()
    }

    override fun stopContainer(
        endpoint: String,
        containerId: String,
    ) {
        dockerClientFactory
            .getClient(endpoint)
            .stopContainerCmd(containerId)
            .exec()
    }

    override fun deleteContainer(
        endpoint: String,
        containerId: String,
    ) {
        dockerClientFactory
            .getClient(endpoint)
            .removeContainerCmd(containerId)
            .withForce(true)
            .exec()
    }

    override fun getLogs(
        endpoint: String,
        containerId: String,
        tail: Int,
    ): String {
        val client = dockerClientFactory.getClient(endpoint)
        val buffer = StringBuilder()

        client
            .logContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .withTail(tail)
            .exec(
                object : ResultCallback.Adapter<Frame>() {
                    override fun onNext(frame: Frame) {
                        buffer.append(String(frame.payload))
                    }
                },
            ).awaitCompletion()

        return buffer.toString()
    }

    override fun isRunning(
        endpoint: String,
        containerId: String,
    ): Boolean =
        dockerClientFactory
            .getClient(endpoint)
            .inspectContainerCmd(containerId)
            .exec()
            .state
            .running ?: false

    override fun getAssignedPort(
        endpoint: String,
        containerId: String,
        internalPort: Int,
    ): Int? {
        val container =
            dockerClientFactory
                .getClient(endpoint)
                .inspectContainerCmd(containerId)
                .exec()

        val bindings = container.networkSettings.ports.bindings
        val exposed = ExposedPort.tcp(internalPort)

        return bindings[exposed]
            ?.firstOrNull()
            ?.hostPortSpec
            ?.toIntOrNull()
    }
}
