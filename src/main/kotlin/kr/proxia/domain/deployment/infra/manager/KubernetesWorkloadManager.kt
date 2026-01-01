package kr.proxia.domain.deployment.infra.manager

import io.fabric8.kubernetes.api.model.IntOrString
import io.fabric8.kubernetes.api.model.ServiceBuilder
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import org.springframework.stereotype.Component

@Component
class KubernetesWorkloadManager(
    private val k8sClient: KubernetesClient,
) {
    fun deployOrUpdate(
        projectName: String,
        serviceName: String,
        imageTag: String,
        port: Int = 8080,
    ) {
        val namespace = "proxia-$projectName"

        k8sClient.apps().deployments().inNamespace(namespace).resource(
            newDeployment(serviceName, imageTag, port)
        ).serverSideApply()

        k8sClient.services().inNamespace(namespace).resource(
            newService(serviceName, port)
        ).serverSideApply()
    }

    private fun newDeployment(name: String, image: String, port: Int) = DeploymentBuilder()
        .withNewMetadata().withName(name).endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withNewSelector().addToMatchLabels("app", name).endSelector()
        .withNewTemplate()
        .withNewMetadata().addToLabels("app", name).endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName(name)
        .withImage(image)
        .addNewPort().withContainerPort(port).endPort()
        .endContainer()
        .endSpec()
        .endTemplate()
        .endSpec()
        .build()

    private fun newService(name: String, port: Int) = ServiceBuilder()
        .withNewMetadata().withName(name).endMetadata()
        .withNewSpec()
        .addToSelector("app", name)
        .addNewPort().withPort(80).withTargetPort(IntOrString(port)).endPort()
        .withType("ClusterIP")
        .endSpec()
        .build()
}
