package kr.proxia.domain.monitoring.application.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.LogContainerCmd
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kr.proxia.domain.container.domain.entity.ContainerEntity
import kr.proxia.domain.container.domain.repository.ContainerRepository

class LogStreamingServiceTest :
    BehaviorSpec({
        val dockerClient = mockk<DockerClient>()
        val containerRepository = mockk<ContainerRepository>()

        val logStreamingService =
            LogStreamingService(
                dockerClient,
                containerRepository,
            )

        Given("streamLogs") {
            val serviceId = 1L
            val tail = 100
            val follow = true

            When("컨테이너를 찾을 수 없는 경우") {
                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns null

                val emitter = logStreamingService.streamLogs(serviceId, tail, follow)

                Then("에러와 함께 emitter 반환") {
                    emitter shouldNotBe null
                }
            }

            When("컨테이너 ID가 null인 경우") {
                val container = mockk<ContainerEntity>(relaxed = true)
                every { container.containerId } returns null

                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns container

                val emitter = logStreamingService.streamLogs(serviceId, tail, follow)

                Then("에러와 함께 emitter 반환") {
                    emitter shouldNotBe null
                }
            }

            When("정상적인 경우") {
                val containerId = "test-container-id"
                val container = mockk<ContainerEntity>(relaxed = true)
                every { container.containerId } returns containerId

                val logContainerCmd = mockk<LogContainerCmd>(relaxed = true)

                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns container
                every { dockerClient.logContainerCmd(containerId) } returns logContainerCmd
                every { logContainerCmd.withStdOut(true) } returns logContainerCmd
                every { logContainerCmd.withStdErr(true) } returns logContainerCmd
                every { logContainerCmd.withTail(tail) } returns logContainerCmd
                every { logContainerCmd.withFollowStream(follow) } returns logContainerCmd
                every { logContainerCmd.exec(any()) } returns mockk(relaxed = true)

                val emitter = logStreamingService.streamLogs(serviceId, tail, follow)

                Then("emitter 반환") {
                    emitter shouldNotBe null
                }
            }

            When("follow가 false인 경우") {
                val containerId = "test-container-id"
                val container = mockk<ContainerEntity>(relaxed = true)
                every { container.containerId } returns containerId

                val logContainerCmd = mockk<LogContainerCmd>(relaxed = true)

                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns container
                every { dockerClient.logContainerCmd(containerId) } returns logContainerCmd
                every { logContainerCmd.withStdOut(true) } returns logContainerCmd
                every { logContainerCmd.withStdErr(true) } returns logContainerCmd
                every { logContainerCmd.withTail(tail) } returns logContainerCmd
                every { logContainerCmd.exec(any()) } returns mockk(relaxed = true)

                val emitter = logStreamingService.streamLogs(serviceId, tail, false)

                Then("emitter 반환 (follow 없이)") {
                    emitter shouldNotBe null
                }
            }
        }

        Given("stopAllStreams") {
            When("모든 스트림 중지") {
                logStreamingService.stopAllStreams()

                Then("예외 없이 완료") {
                    // No exception should be thrown
                    true shouldBe true
                }
            }
        }
    })
