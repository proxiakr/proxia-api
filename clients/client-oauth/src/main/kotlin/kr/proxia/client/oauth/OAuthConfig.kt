package kr.proxia.client.oauth

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(OAuthProperties::class)
class OAuthConfig {
    @Bean
    fun googleRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl("https://oauth2.googleapis.com")
            .build()

    @Bean
    fun googleApiRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl("https://www.googleapis.com")
            .build()

    @Bean
    fun githubRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl("https://github.com")
            .build()

    @Bean
    fun githubApiRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl("https://api.github.com")
            .build()
}
