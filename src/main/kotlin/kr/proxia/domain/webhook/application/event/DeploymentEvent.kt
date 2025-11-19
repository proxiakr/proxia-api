package kr.proxia.domain.webhook.application.event

import java.util.UUID

data class DeploymentEvent(
    val serviceId: UUID,
    val projectId: UUID,
    val gitUrl: String,
    val branch: String,
    val commitSha: String,
    val commitMessage: String?,
    val commitAuthor: String?,
)
