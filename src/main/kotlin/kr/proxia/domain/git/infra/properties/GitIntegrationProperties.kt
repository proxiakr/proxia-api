package kr.proxia.domain.git.infra.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "git.integration")
data class GitIntegrationProperties(
    val github: Github,
) {
    data class Github(
        val clientId: String,
        val clientSecret: String,
    )
}