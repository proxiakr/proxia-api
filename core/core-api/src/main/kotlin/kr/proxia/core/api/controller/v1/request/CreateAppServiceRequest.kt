package kr.proxia.core.api.controller.v1.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.proxia.core.domain.CreateAppService
import kr.proxia.core.enums.Framework
import java.util.UUID

data class CreateAppServiceRequest(
    @field:NotBlank
    val name: String,
    @field:NotNull
    val x: Double,
    @field:NotNull
    val y: Double,
    @field:NotBlank
    val repoFullName: String,
    val branch: String?,
    val port: Int?,
    @field:NotNull
    val framework: Framework,
    val buildCommand: String?,
    val startCommand: String?,
    @field:NotBlank
    val gitConnectionId: UUID,
) {
    fun toDomain(): CreateAppService =
        CreateAppService(
            name = name,
            x = x,
            y = y,
            repoFullName = repoFullName,
            branch = branch,
            port = port,
            framework = framework,
            buildCommand = buildCommand,
            startCommand = startCommand,
            gitConnectionId = gitConnectionId,
        )
}
