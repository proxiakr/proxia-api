package kr.proxia.domain.monitoring.application.service

import kr.proxia.domain.container.domain.repository.ContainerRepository
import kr.proxia.domain.monitoring.presentation.response.ServiceHealthResponse
import kr.proxia.global.docker.service.DockerService
import org.springframework.stereotype.Service

@Service
class HealthCheckService(
    private val containerRepository: ContainerRepository,
    private val dockerService: DockerService,
    private val containerMetricsService: ContainerMetricsService,
) {
    fun getServiceHealth(serviceId: Long): ServiceHealthResponse? {
        val container =
            containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId)
                ?: return null

        val containerId = container.containerId
        val isRunning = containerId?.let { dockerService.isContainerRunning(it) } ?: false
        val metrics = containerMetricsService.getContainerMetrics(serviceId)

        return ServiceHealthResponse(
            serviceId = serviceId,
            containerId = containerId,
            status = container.status.name,
            isRunning = isRunning,
            metrics = metrics,
        )
    }
}
