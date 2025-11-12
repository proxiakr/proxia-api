package kr.proxia.domain.monitoring.presentation.response

data class ContainerMetricsResponse(
    val cpuUsagePercent: Double,
    val memoryUsageBytes: Long,
    val memoryLimitBytes: Long,
    val memoryUsagePercent: Double,
    val networkRxBytes: Long,
    val networkTxBytes: Long,
)
