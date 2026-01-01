package kr.proxia.domain.deployment.infra.builder

interface ImageBuilder {
    suspend fun build(
        projectName: String,
        url: String,
        targetImage: String,
        deploymentId: String,
        serviceName: String,
    ): String

    fun supports(request: BuildRequest): Boolean
}
