package kr.proxia.client.git

import kr.proxia.client.git.github.GitHubProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "git")
data class GitProperties(
    val github: GitHubProperties,
)
