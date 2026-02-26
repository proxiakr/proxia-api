package kr.proxia.client.git

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(GitProperties::class)
class GitConfig {
    @Bean
    fun githubRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl("https://github.com")
            .build()
}
