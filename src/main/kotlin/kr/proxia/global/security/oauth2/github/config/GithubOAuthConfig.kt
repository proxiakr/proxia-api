package kr.proxia.global.security.oauth2.github.config

import kr.proxia.global.security.oauth2.github.properties.GithubOAuthProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(GithubOAuthProperties::class)
class GithubOAuthConfig