package kr.proxia.domain.deployment.infra.informer

import io.fabric8.kubernetes.api.model.GenericKubernetesResource
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import kr.proxia.domain.deployment.domain.enums.DeploymentStatus
import kr.proxia.domain.deployment.domain.repository.DeploymentRepository
import kr.proxia.domain.deployment.infra.manager.KubernetesWorkloadManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class KpackImageInformer(
    private val k8sClient: KubernetesClient,
    private val kubernetesWorkloadManager: KubernetesWorkloadManager,
    private val deploymentRepository: DeploymentRepository,
) {
    private val informer = k8sClient.genericKubernetesResources("kpack.io/v1alpha2", "Image")
        .inAnyNamespace()
        .inform()

    init {
        informer.addEventHandler(object : ResourceEventHandler<GenericKubernetesResource> {
            override fun onUpdate(oldObj: GenericKubernetesResource, newObj: GenericKubernetesResource) {
                val deploymentId = newObj.metadata.annotations["proxia.kr/deployment-id"] ?: return

                if (isReady(newObj)) {
                    updateDeploymentStatus(deploymentId, DeploymentStatus.SUCCESS, newObj)
                } else if (isFailed(newObj)) {
                    updateDeploymentStatus(deploymentId, DeploymentStatus.FAILED, newObj)
                }
            }

            override fun onAdd(obj: GenericKubernetesResource) {}
            override fun onDelete(obj: GenericKubernetesResource, status: Boolean) {}
        })
    }

    private fun updateDeploymentStatus(id: String, status: DeploymentStatus, newObj: GenericKubernetesResource) {
        val deployment = deploymentRepository.findByIdOrNull(UUID.fromString(id)) ?: return
        if (deployment.status == status) return

        deployment.update(status = status)
        deploymentRepository.save(deployment)

        if (status == DeploymentStatus.SUCCESS) {
            val finalImage = extractLatestImage(newObj)

            val serviceName = newObj.metadata.labels["proxia.kr/service-name"] ?: "default-app"
            val projectName = newObj.metadata.namespace.replace("proxia-", "")

            kubernetesWorkloadManager.deployOrUpdate(
                projectName = projectName,
                serviceName = serviceName,
                imageTag = finalImage
            )
        }
    }

    private fun extractLatestImage(resource: GenericKubernetesResource): String {
        val status = resource.additionalProperties["status"] as? Map<*, *>
        return status?.get("latestImage") as? String
            ?: throw IllegalStateException("latestImage not found in kpack image status")
    }

    private fun isReady(res: GenericKubernetesResource): Boolean {
        val status = res.additionalProperties["status"] as? Map<*, *> ?: return false
        val conditions = status["conditions"] as? List<Map<String, String>> ?: return false

        return conditions.any { it["type"] == "Ready" && it["status"] == "True" }
    }

    private fun isFailed(res: GenericKubernetesResource): Boolean {
        val status = res.additionalProperties["status"] as? Map<*, *> ?: return false
        val conditions = status["conditions"] as? List<Map<String, String>> ?: return false

        return conditions.any { it["type"] == "Ready" && it["status"] == "False" && it["reason"]?.contains("Error") == true }
    }
}
