package kr.proxia.global.github.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "github")
data class GithubProperties(
    val username: String,
    val pat: String,
)
