package kr.proxia.domain.monitoring.application.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Statistics
import kr.proxia.domain.container.domain.repository.ContainerRepository
import kr.proxia.domain.monitoring.presentation.response.ContainerMetricsResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ContainerMetricsService(
    private val dockerClient: DockerClient,
    private val containerRepository: ContainerRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getContainerMetrics(serviceId: Long): ContainerMetricsResponse? {
        val container =
            containerRepository.findByServiceIdAndDeletedAtIsNull(serviceId)
                ?: return null

        val containerId = container.containerId ?: return null

        return try {
            var metrics: ContainerMetricsResponse? = null

            val callback =
                object : ResultCallback.Adapter<Statistics>() {
                    override fun onNext(stats: Statistics) {
                        metrics = calculateMetrics(stats)
                        close()
                    }
                }

            dockerClient
                .statsCmd(containerId)
                .withNoStream(true)
                .exec(callback)
                .awaitCompletion()

            metrics
        } catch (e: Exception) {
            logger.error("Failed to get metrics for container $containerId", e)
            null
        }
    }

    private fun calculateMetrics(stats: Statistics): ContainerMetricsResponse {
        val cpuUsage = calculateCpuUsage(stats)
        val memoryUsage = calculateMemoryUsage(stats)
        val networkUsage = calculateNetworkUsage(stats)

        return ContainerMetricsResponse(
            cpuUsagePercent = cpuUsage,
            memoryUsageBytes = stats.memoryStats?.usage ?: 0L,
            memoryLimitBytes = stats.memoryStats?.limit ?: 0L,
            memoryUsagePercent = memoryUsage,
            networkRxBytes = networkUsage.first,
            networkTxBytes = networkUsage.second,
        )
    }

    private fun calculateCpuUsage(stats: Statistics): Double {
        val cpuDelta =
            (stats.cpuStats?.cpuUsage?.totalUsage ?: 0L) -
                (stats.preCpuStats?.cpuUsage?.totalUsage ?: 0L)

        val systemDelta =
            (stats.cpuStats?.systemCpuUsage ?: 0L) -
                (stats.preCpuStats?.systemCpuUsage ?: 0L)

        val cpuCount =
            stats.cpuStats
                ?.cpuUsage
                ?.percpuUsage
                ?.size ?: 1

        return if (systemDelta > 0 && cpuDelta > 0) {
            (cpuDelta.toDouble() / systemDelta.toDouble()) * cpuCount * 100.0
        } else {
            0.0
        }
    }

    private fun calculateMemoryUsage(stats: Statistics): Double {
        val usage = stats.memoryStats?.usage ?: 0L
        val limit = stats.memoryStats?.limit ?: 1L

        return if (limit > 0) {
            (usage.toDouble() / limit.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    private fun calculateNetworkUsage(stats: Statistics): Pair<Long, Long> {
        var rxBytes = 0L
        var txBytes = 0L

        stats.networks?.values?.forEach { network ->
            rxBytes += network.rxBytes ?: 0L
            txBytes += network.txBytes ?: 0L
        }

        return Pair(rxBytes, txBytes)
    }
}
