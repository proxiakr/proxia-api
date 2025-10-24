package kr.proxia.domain.git.infra.client

import kr.proxia.domain.git.infra.data.GithubRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux

@Component
class GithubRepositoryClient(private val webClient: WebClient) {
    fun getGithubRepositories(accessToken: String): List<GithubRepository> {
        val repositories = webClient.get()
            .uri("https://api.github.com/user/repos")
            .headers { headers ->
                headers.setBearerAuth(accessToken)
                headers.set("Accept", "application/vnd.github.v3+json")
            }
            .retrieve()
            .bodyToFlux<GithubRepository>()
            .collectList()
            .block() ?: throw IllegalStateException("Failed to fetch repositories from GitHub")

        return repositories
    }
}