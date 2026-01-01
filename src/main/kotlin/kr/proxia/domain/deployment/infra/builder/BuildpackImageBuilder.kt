package kr.proxia.domain.deployment.infra.builder

import io.fabric8.kubernetes.client.KubernetesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BuildpackImageBuilder(
    private val k8sClient: KubernetesClient,
) : ImageBuilder {
    override suspend fun build(
        projectName: String,
        url: String,
        targetImage: String,
        deploymentId: String,
        serviceName: String,
    ): String =
        withContext(Dispatchers.IO) {
            val buildId = UUID.randomUUID().toString().take(8)
            val resourceName = "build-$buildId"

            val resource = k8sClient.genericKubernetesResources("kpack.io/v1alpha2", "Image")
                .inNamespace("proxia-$projectName")
                .load(createImageManifest(resourceName, targetImage, url, deploymentId, serviceName))

            try {
                val created = resource.create()

                // 2. 필요하다면 빌드 완료까지 대기하는 로직 추가
                // waitForBuildComplete(resourceName)

                created.metadata.name
            } catch (e: Exception) {
                throw RuntimeException("Failed to create kpack image resource: ${e.message}", e)
            }
        }

    override fun supports(request: BuildRequest): Boolean = !request.hasDockerfile

    private fun createImageManifest(
        name: String,
        tag: String,
        gitUrl: String,
        deploymentId: String,
        serviceName: String,
    ): String = """
        apiVersion: kpack.io/v1alpha2
        kind: Image
        metadata:
          name: $name
          labels:
            proxia.kr/service-name: "$serviceName"
          annotations:
            proxia.kr/deployment-id: "$deploymentId"
        spec:
          tag: $tag
          serviceAccountName: kpack-service-account
          builder:
            name: base-builder
            kind: ClusterBuilder
          source:
            git:
              url: $gitUrl
              revision: main
    """.trimIndent()
}
