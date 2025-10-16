package kr.proxia.global.security.oauth2.github.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2.github")
data class GithubOAuthProperties(
    val clientId: String,
    val clientSecret: String,
)