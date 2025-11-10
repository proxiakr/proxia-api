package kr.proxia.domain.resource.presentation.response

import kr.proxia.domain.resource.domain.entity.AppResourceEntity
import kr.proxia.domain.service.domain.enums.AppFramework

data class AppResourceResponse(
    val framework: AppFramework?,
    val repositoryUrl: String?,
    val branch: String?,
    val rootDirectory: String?,
    val buildCommand: String?,
    val installCommand: String?,
    val startCommand: String?,
    val envVariables: String?,
) {
    companion object {
        fun of(appResource: AppResourceEntity) =
            AppResourceResponse(
                framework = appResource.framework,
                repositoryUrl = appResource.repositoryUrl,
                branch = appResource.branch,
                rootDirectory = appResource.rootDirectory,
                buildCommand = appResource.buildCommand,
                installCommand = appResource.installCommand,
                startCommand = appResource.startCommand,
                envVariables = appResource.envVariables,
            )
    }
}
