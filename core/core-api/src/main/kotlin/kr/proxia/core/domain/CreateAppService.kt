package kr.proxia.core.domain

import kr.proxia.core.enums.Framework
import java.util.UUID

data class CreateAppService(
    val name: String,
    val x: Double,
    val y: Double,
    val repoFullName: String,
    val branch: String?,
    val port: Int?,
    val framework: Framework,
    val buildCommand: String?,
    val startCommand: String?,
    val gitConnectionId: UUID,
)
