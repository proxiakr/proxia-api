package kr.proxia.core.domain

import java.util.UUID

object DockerNaming {
    fun network(projectId: UUID) = "proxia-net-$projectId"

    fun container(serviceId: UUID) = "proxia-$serviceId"

    fun container(
        serviceId: UUID,
        deploymentId: UUID,
    ) = "proxia-$serviceId-$deploymentId"

    fun volume(serviceId: UUID) = "proxia-vol-$serviceId"
}
