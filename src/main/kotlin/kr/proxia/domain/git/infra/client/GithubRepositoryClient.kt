package kr.proxia.domain.git.infra.client

import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.infra.data.GithubRepository
import kr.proxia.global.error.BusinessException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToFlux

@Component
class GithubRepositoryClient(
    private val webClient: WebClient,
) {
    fun getGithubRepositories(accessToken: String): List<GithubRepository> =
        try {
            webClient
                .get()
                .uri("https://api.github.com/user/repos")
                .headers { headers ->
                    headers.setBearerAuth(accessToken)
                    headers.set("Accept", "application/vnd.github.v3+json")
                }.retrieve()
                .bodyToFlux<GithubRepository>()
                .collectList()
                .block() ?: throw BusinessException(GitError.GithubApiError)
        } catch (e: WebClientResponseException) {
            when (e.statusCode.value()) {
                401 -> throw BusinessException(GitError.AccessDenied)
                else -> throw BusinessException(GitError.GithubApiError)
            }
        } catch (_: Exception) {
            throw BusinessException(GitError.GithubApiError)
        }
}
