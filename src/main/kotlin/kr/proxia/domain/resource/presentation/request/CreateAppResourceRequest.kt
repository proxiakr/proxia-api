package kr.proxia.domain.resource.presentation.request

import kr.proxia.domain.service.domain.enums.AppFramework
import java.util.UUID

data class CreateAppResourceRequest(
    val framework: AppFramework?,
    val repositoryUrl: String?,
    val branch: String?,
    val rootDirectory: String?,
    val buildCommand: String?,
    val installCommand: String?,
    val startCommand: String?,
    val envVariables: String?,
    val gitRepositoryId: UUID?,
)
