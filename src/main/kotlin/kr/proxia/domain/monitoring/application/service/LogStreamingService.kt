package kr.proxia.domain.monitoring.application.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import kr.proxia.domain.container.domain.repository.ContainerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Service
class LogStreamingService(
    private val dockerClient: DockerClient,
    private val containerRepository: ContainerRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val activeStreams = ConcurrentHashMap<String, ResultCallback<Frame>>()

    fun streamLogs(
        serviceId: Long,
        tail: Int = 100,
        follow: Boolean = true,
    ): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        val container = containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId)

        if (container == null) {
            emitter.completeWithError(IllegalArgumentException("Container not found for service $serviceId"))
            return emitter
        }

        val containerId = container.containerId
        if (containerId == null) {
            emitter.completeWithError(IllegalArgumentException("Container ID is null for service $serviceId"))
            return emitter
        }

        try {
            val logCallback =
                object : ResultCallback.Adapter<Frame>() {
                    override fun onNext(frame: Frame) {
                        try {
                            val log = String(frame.payload).trim()
                            if (log.isNotEmpty()) {
                                emitter.send(
                                    SseEmitter
                                        .event()
                                        .name("log")
                                        .data(log),
                                )
                            }
                        } catch (e: IOException) {
                            logger.warn("Failed to send log to SSE emitter", e)
                            close()
                        }
                    }

                    override fun onComplete() {
                        try {
                            emitter.complete()
                        } catch (e: Exception) {
                            logger.warn("Failed to complete SSE emitter", e)
                        }
                        activeStreams.remove(containerId)
                    }

                    override fun onError(throwable: Throwable) {
                        try {
                            emitter.completeWithError(throwable)
                        } catch (e: Exception) {
                            logger.warn("Failed to complete SSE emitter with error", e)
                        }
                        activeStreams.remove(containerId)
                    }
                }

            val logCmd =
                dockerClient
                    .logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTail(tail)

            if (follow) {
                logCmd.withFollowStream(true)
            }

            logCmd.exec(logCallback)

            activeStreams[containerId] = logCallback

            emitter.onCompletion {
                logger.info("SSE connection completed for container $containerId")
                stopStreaming(containerId)
            }

            emitter.onTimeout {
                logger.info("SSE connection timed out for container $containerId")
                stopStreaming(containerId)
            }

            emitter.onError {
                logger.warn("SSE connection error for container $containerId", it)
                stopStreaming(containerId)
            }
        } catch (e: Exception) {
            logger.error("Failed to start log streaming for container $containerId", e)
            emitter.completeWithError(e)
        }

        return emitter
    }

    private fun stopStreaming(containerId: String) {
        activeStreams[containerId]?.let { callback ->
            try {
                callback.close()
                activeStreams.remove(containerId)
                logger.info("Stopped log streaming for container $containerId")
            } catch (e: Exception) {
                logger.warn("Failed to stop log streaming for container $containerId", e)
            }
        }
    }

    fun stopAllStreams() {
        activeStreams.keys.forEach { containerId ->
            stopStreaming(containerId)
        }
    }
}
