package kr.proxia.domain.monitoring.presentation.response

data class ServiceHealthResponse(
    val serviceId: Long,
    val containerId: String?,
    val status: String,
    val isRunning: Boolean,
    val metrics: ContainerMetricsResponse?,
)
