package kr.proxia.domain.monitoring.application.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.StatsCmd
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.proxia.domain.container.domain.entity.ContainerEntity
import kr.proxia.domain.container.domain.repository.ContainerRepository

class ContainerMetricsServiceTest :
    BehaviorSpec({
        val dockerClient = mockk<DockerClient>()
        val containerRepository = mockk<ContainerRepository>()

        val containerMetricsService =
            ContainerMetricsService(
                dockerClient,
                containerRepository,
            )

        Given("getContainerMetrics") {
            val serviceId = 1L

            When("컨테이너를 찾을 수 없는 경우") {
                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns null

                val result = containerMetricsService.getContainerMetrics(serviceId)

                Then("null 반환") {
                    result shouldBe null
                }
            }

            When("컨테이너 ID가 null인 경우") {
                val container = mockk<ContainerEntity>(relaxed = true)
                every { container.containerId } returns null

                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns container

                val result = containerMetricsService.getContainerMetrics(serviceId)

                Then("null 반환") {
                    result shouldBe null
                }
            }

            When("Docker stats 조회 실패") {
                val containerId = "test-container-id"
                val container = mockk<ContainerEntity>(relaxed = true)
                every { container.containerId } returns containerId

                val statsCmd = mockk<StatsCmd>(relaxed = true)

                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns container
                every { dockerClient.statsCmd(containerId) } returns statsCmd
                every { statsCmd.withNoStream(true) } returns statsCmd
                every { statsCmd.exec(any()) } throws RuntimeException("Docker error")

                val result = containerMetricsService.getContainerMetrics(serviceId)

                Then("null 반환") {
                    result shouldBe null
                }
            }
        }
    })
