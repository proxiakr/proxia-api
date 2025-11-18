package kr.proxia.domain.monitoring.presentation.response

import java.util.UUID

data class ServiceHealthResponse(
    val serviceId: UUID,
    val containerId: String?,
    val status: String,
    val isRunning: Boolean,
    val metrics: ContainerMetricsResponse?,
)
