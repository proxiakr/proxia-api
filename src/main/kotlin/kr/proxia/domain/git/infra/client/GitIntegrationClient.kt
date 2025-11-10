package kr.proxia.domain.git.infra.client

import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.infra.data.ExchangeGithubCodeResponse
import kr.proxia.domain.git.infra.properties.GitIntegrationProperties
import kr.proxia.global.error.BusinessException
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class GitIntegrationClient(
    private val webClient: WebClient,
    private val gitIntegrationProperties: GitIntegrationProperties,
) {
    fun exchangeGithubCode(code: String): ExchangeGithubCodeResponse =
        try {
            webClient
                .post()
                .uri {
                    it
                        .scheme("https")
                        .host("github.com")
                        .path("/login/oauth/access_token")
                        .queryParam("client_id", gitIntegrationProperties.github.clientId)
                        .queryParam("client_secret", gitIntegrationProperties.github.clientSecret)
                        .queryParam("code", code)
                        .build()
                }.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono<ExchangeGithubCodeResponse>()
                .block() ?: throw BusinessException(GitError.INVALID_GITHUB_CODE)
        } catch (e: WebClientResponseException) {
            when (e.statusCode.value()) {
                400, 401 -> throw BusinessException(GitError.INVALID_GITHUB_CODE)
                else -> throw BusinessException(GitError.GITHUB_API_ERROR)
            }
        } catch (_: Exception) {
            throw BusinessException(GitError.GITHUB_API_ERROR)
        }
}
