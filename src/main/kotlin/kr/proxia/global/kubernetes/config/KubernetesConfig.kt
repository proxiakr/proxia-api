package kr.proxia.global.kubernetes.config

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import kr.proxia.global.kubernetes.properties.KubernetesProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KubernetesProperties::class)
class KubernetesConfig(
    private val kubernetesProperties: KubernetesProperties,
) {
    @Bean
    fun kubernetesClient(): KubernetesClient =
        kubernetesProperties.configPath?.let {
            KubernetesClientBuilder()
                .withConfig(Config.fromKubeconfig(it))
                .build()
        } ?: KubernetesClientBuilder().build()
}
