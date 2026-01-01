package kr.proxia.global.kubernetes

import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import kr.proxia.global.github.properties.GithubProperties
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Base64

@Configuration
class KubernetesConfig(
    private val githubProperties: GithubProperties,
) {
    @Bean
    fun kubernetesClient(): KubernetesClient =
        KubernetesClientBuilder().apply {
            withConfig(ConfigBuilder().apply {
                withConnectionTimeout(5000)
                withRequestTimeout(10000)
            }.build())
        }.build()

    @Bean
    fun setupRegistrySecret(client: KubernetesClient): CommandLineRunner {
        return CommandLineRunner {
            val secretName = "ghcr-auth"

            val existingSecret = client.secrets().inNamespace("default").withName(secretName).get()

            if (existingSecret == null) {
                val auth = Base64.getEncoder().encodeToString("$username:$pat".toByteArray())
                val dockerConfig = """
                    {
                        "auths": {
                            "ghcr.io": {
                                "auth": "$auth"
                            }
                        }
                    }
                """.trimIndent()

                client.secrets().inNamespace("default").resource(
                    SecretBuilder()
                        .withNewMetadata().withName(secretName).endMetadata()
                        .withType("kubernetes.io/dockerconfigjson")
                        .addToData(".dockerconfigjson", Base64.getEncoder().encodeToString(dockerConfig.toByteArray()))
                        .build()
                ).create()

                println("")
            }
        }
    }
}
