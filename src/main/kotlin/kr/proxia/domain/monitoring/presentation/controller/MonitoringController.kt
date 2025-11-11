package kr.proxia.domain.monitoring.presentation.controller

import kr.proxia.domain.monitoring.application.service.ContainerMetricsService
import kr.proxia.domain.monitoring.application.service.HealthCheckService
import kr.proxia.domain.monitoring.presentation.response.ContainerMetricsResponse
import kr.proxia.domain.monitoring.presentation.response.ServiceHealthResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/services")
class MonitoringController(
    private val healthCheckService: HealthCheckService,
    private val containerMetricsService: ContainerMetricsService,
) {
    @GetMapping("/{serviceId}/health")
    fun getServiceHealth(
        @PathVariable serviceId: Long,
    ): ResponseEntity<ServiceHealthResponse> {
        val health =
            healthCheckService.getServiceHealth(serviceId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(health)
    }

    @GetMapping("/{serviceId}/metrics")
    fun getServiceMetrics(
        @PathVariable serviceId: Long,
    ): ResponseEntity<ContainerMetricsResponse> {
        val metrics =
            containerMetricsService.getContainerMetrics(serviceId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(metrics)
    }
}
