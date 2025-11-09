package kr.proxia.domain.git.infra.client

import kr.proxia.domain.auth.domain.error.AuthError
import kr.proxia.domain.git.infra.data.ExchangeGithubCodeResponse
import kr.proxia.domain.git.infra.properties.GitIntegrationProperties
import kr.proxia.global.error.BusinessException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class GitIntegrationClient(
    private val webClient: WebClient,
    private val gitIntegrationProperties: GitIntegrationProperties,
) {
    fun exchangeGithubCode(code: String): ExchangeGithubCodeResponse {
        val response =
            webClient
                .post()
                .uri(
                    "https://github.com/login/oauth/access_token?client_id={clientId}&client_secret={clientSecret}&code={code}",
                    gitIntegrationProperties.github.clientId,
                    gitIntegrationProperties.github.clientSecret,
                    code,
                ).header("Accept", "application/json")
                .retrieve()
                .bodyToMono<ExchangeGithubCodeResponse>()
                .block() ?: throw BusinessException(AuthError.INVALID_TOKEN)

        return response
    }
}
