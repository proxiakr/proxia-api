package kr.proxia.global.container.docker.runtime

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import kr.proxia.global.container.ContainerRuntime
import org.springframework.stereotype.Component

@Component
class DockerRuntime(
    private val dockerClient: DockerClient,
) : ContainerRuntime {
    override fun exec(
        containerName: String,
        command: List<String>,
    ) {
        val exec =
            dockerClient
                .execCreateCmd(containerName)
                .withCmd(*command.toTypedArray())
                .exec()

        dockerClient
            .execStartCmd(exec.id)
            .exec(object : ResultCallback.Adapter<Frame>() {})
            .awaitCompletion()
    }
}
