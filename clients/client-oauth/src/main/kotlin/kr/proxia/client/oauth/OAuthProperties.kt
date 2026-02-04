package kr.proxia.client.oauth

import kr.proxia.client.oauth.github.GitHubProperties
import kr.proxia.client.oauth.google.GoogleProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth")
data class OAuthProperties(
    val google: GoogleProperties,
    val github: GitHubProperties,
)
