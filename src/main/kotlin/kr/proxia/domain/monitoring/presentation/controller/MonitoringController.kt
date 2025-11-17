package kr.proxia.domain.monitoring.presentation.controller

import kr.proxia.domain.monitoring.application.service.ContainerMetricsService
import kr.proxia.domain.monitoring.presentation.docs.MonitoringDocs
import kr.proxia.domain.node.application.service.NodeHealthCheckService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/services/{serviceId}")
class MonitoringController(
    private val containerMetricsService: ContainerMetricsService,
    private val healthCheckService: NodeHealthCheckService,
) : MonitoringDocs {
}
