package kr.proxia.global.kubernetes.service

import io.fabric8.kubernetes.api.model.ContainerBuilder
import io.fabric8.kubernetes.api.model.EnvVar
import io.fabric8.kubernetes.api.model.IntOrString
import io.fabric8.kubernetes.api.model.PodSpecBuilder
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.ServiceBuilder
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import kr.proxia.global.kubernetes.properties.KubernetesProperties
import org.springframework.stereotype.Service as SpringService

@SpringService
class KubernetesService(
    private val kubernetesClient: KubernetesClient,
    private val kubernetesProperties: KubernetesProperties,
) {
    fun deploy(
        name: String,
        image: String,
        replicas: Int = 1,
        env: Map<String, String> = emptyMap(),
        memory: String? = null,
        cpu: String? = null,
    ): Deployment {
        val deployment = createDeployment(name, image, replicas, env, memory, cpu)
        val service = createService(name)

        kubernetesClient
            .apps()
            .deployments()
            .inNamespace(kubernetesProperties.namespace)
            .resource(deployment)
            .serverSideApply()

        kubernetesClient
            .services()
            .inNamespace(kubernetesProperties.namespace)
            .resource(service)
            .serverSideApply()

        return deployment
    }

    fun delete(name: String) {
        kubernetesClient
            .apps()
            .deployments()
            .inNamespace(kubernetesProperties.namespace)
            .withName(name)
            .delete()

        kubernetesClient
            .services()
            .inNamespace(kubernetesProperties.namespace)
            .withName(name)
            .delete()
    }

    fun scale(
        name: String,
        replicas: Int,
    ) {
        kubernetesClient
            .apps()
            .deployments()
            .inNamespace(kubernetesProperties.namespace)
            .withName(name)
            .scale(replicas)
    }

    fun logs(
        name: String,
        tail: Int = 100,
    ): String =
        kubernetesClient
            .pods()
            .inNamespace(kubernetesProperties.namespace)
            .withLabel("app", name)
            .list()
            .items
            .firstOrNull()
            ?.let { pod ->
                kubernetesClient
                    .pods()
                    .inNamespace(kubernetesProperties.namespace)
                    .withName(pod.metadata.name)
                    .tailingLines(tail)
                    .log
            } ?: ""

    fun isRunning(name: String): Boolean =
        kubernetesClient
            .apps()
            .deployments()
            .inNamespace(kubernetesProperties.namespace)
            .withName(name)
            .get()
            ?.status
            ?.readyReplicas
            ?.let { it > 0 } ?: false

    private fun createDeployment(
        name: String,
        image: String,
        replicas: Int,
        env: Map<String, String>,
        memory: String?,
        cpu: String?,
    ): Deployment {
        val limits = mutableMapOf<String, Quantity>()
        memory?.let { limits["memory"] = Quantity(it) }
        cpu?.let { limits["cpu"] = Quantity(it) }

        val resources =
            if (limits.isNotEmpty()) {
                io.fabric8.kubernetes.api.model
                    .ResourceRequirementsBuilder()
                    .withLimits<String, Quantity>(limits)
                    .build()
            } else {
                null
            }

        val container =
            ContainerBuilder()
                .withName(name)
                .withImage(image)
                .withEnv(env.map { EnvVar(it.key, it.value, null) })
                .apply { resources?.let { withResources(it) } }
                .build()

        val podSpec =
            PodSpecBuilder()
                .withContainers(container)
                .build()

        val podTemplate =
            PodTemplateSpecBuilder()
                .withNewMetadata()
                .withLabels<String, String>(mapOf("app" to name))
                .endMetadata()
                .withSpec(podSpec)
                .build()

        return DeploymentBuilder()
            .withNewMetadata()
            .withName(name)
            .withLabels<String, String>(mapOf("app" to name))
            .endMetadata()
            .withNewSpec()
            .withReplicas(replicas)
            .withNewSelector()
            .withMatchLabels<String, String>(mapOf("app" to name))
            .endSelector()
            .withTemplate(podTemplate)
            .endSpec()
            .build()
    }

    private fun createService(name: String): Service {
        val servicePort =
            io.fabric8.kubernetes.api.model
                .ServicePortBuilder()
                .withPort(80)
                .withTargetPort(IntOrString(8080))
                .build()

        return ServiceBuilder()
            .withNewMetadata()
            .withName(name)
            .withLabels<String, String>(mapOf("app" to name))
            .endMetadata()
            .withNewSpec()
            .withSelector<String, String>(mapOf("app" to name))
            .withPorts(servicePort)
            .endSpec()
            .build()
    }
}
