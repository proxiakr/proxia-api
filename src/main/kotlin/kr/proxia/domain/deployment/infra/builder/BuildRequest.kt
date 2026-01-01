package kr.proxia.domain.deployment.infra.builder

import kr.proxia.domain.service.domain.enums.AppFramework

data class BuildRequest(
    val url: String,
    val branch: String = "main",
    val appName: String,
    val version: String,
    val framework: AppFramework,
    val hasDockerfile: Boolean = false,
) {
    fun getFullImageName(registry: String, userId: String): String =
        "$registry/$userId/$appName:$version"
}
