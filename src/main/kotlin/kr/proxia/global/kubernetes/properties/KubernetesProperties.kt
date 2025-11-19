package kr.proxia.global.kubernetes.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kubernetes")
data class KubernetesProperties(
    val namespace: String = "default",
    val configPath: String? = null,
)
