package kr.proxia.domain.monitoring.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.proxia.domain.container.domain.entity.ContainerEntity
import kr.proxia.domain.container.domain.enums.ContainerStatus
import kr.proxia.domain.container.domain.repository.ContainerRepository
import kr.proxia.domain.monitoring.presentation.response.ContainerMetricsResponse
import kr.proxia.global.docker.service.DockerService

class HealthCheckServiceTest :
    BehaviorSpec({
        val containerRepository = mockk<ContainerRepository>()
        val dockerService = mockk<DockerService>()
        val containerMetricsService = mockk<ContainerMetricsService>()

        val healthCheckService =
            HealthCheckService(
                containerRepository,
                dockerService,
                containerMetricsService,
            )

        Given("getServiceHealth") {
            val serviceId = 1L

            When("컨테이너를 찾을 수 없는 경우") {
                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns null

                val result = healthCheckService.getServiceHealth(serviceId)

                Then("null 반환") {
                    result shouldBe null
                }
            }

            When("컨테이너가 실행 중인 경우") {
                val containerId = "test-container-id"
                val container = mockk<ContainerEntity>(relaxed = true)
                every { container.containerId } returns containerId
                every { container.status } returns ContainerStatus.RUNNING

                val metrics =
                    ContainerMetricsResponse(
                        cpuUsagePercent = 50.0,
                        memoryUsageBytes = 100000000L,
                        memoryLimitBytes = 200000000L,
                        memoryUsagePercent = 50.0,
                        networkRxBytes = 1000L,
                        networkTxBytes = 2000L,
                    )

                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns container
                every { dockerService.isContainerRunning(containerId) } returns true
                every { containerMetricsService.getContainerMetrics(serviceId) } returns metrics

                val result = healthCheckService.getServiceHealth(serviceId)

                Then("헬스체크 정보 반환") {
                    result?.serviceId shouldBe serviceId
                    result?.containerId shouldBe containerId
                    result?.status shouldBe "RUNNING"
                    result?.isRunning shouldBe true
                    result?.metrics shouldBe metrics
                }
            }

            When("컨테이너가 중지된 경우") {
                val containerId = "test-container-id"
                val container = mockk<ContainerEntity>(relaxed = true)
                every { container.containerId } returns containerId
                every { container.status } returns ContainerStatus.STOPPED

                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns container
                every { dockerService.isContainerRunning(containerId) } returns false
                every { containerMetricsService.getContainerMetrics(serviceId) } returns null

                val result = healthCheckService.getServiceHealth(serviceId)

                Then("중지 상태로 반환") {
                    result?.serviceId shouldBe serviceId
                    result?.isRunning shouldBe false
                    result?.status shouldBe "STOPPED"
                }
            }

            When("컨테이너 ID가 null인 경우") {
                val container = mockk<ContainerEntity>(relaxed = true)
                every { container.containerId } returns null
                every { container.status } returns ContainerStatus.BUILDING

                every { containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId) } returns container
                every { containerMetricsService.getContainerMetrics(serviceId) } returns null

                val result = healthCheckService.getServiceHealth(serviceId)

                Then("헬스체크 정보 반환 (isRunning = false)") {
                    result?.serviceId shouldBe serviceId
                    result?.containerId shouldBe null
                    result?.isRunning shouldBe false
                }
            }
        }
    })
