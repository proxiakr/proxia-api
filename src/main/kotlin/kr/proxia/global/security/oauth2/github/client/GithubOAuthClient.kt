package kr.proxia.global.security.oauth2.github.client

import kr.proxia.domain.auth.domain.error.AuthError
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.oauth2.github.data.GithubTokenResponse
import kr.proxia.global.security.oauth2.github.data.GithubUserInfo
import kr.proxia.global.security.oauth2.github.properties.GithubOAuthProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class GithubOAuthClient(
    private val webClient: WebClient,
    private val githubOAuthProperties: GithubOAuthProperties,
) {
    fun getUserInfo(code: String): GithubUserInfo {
        val accessToken =
            webClient
                .post()
                .uri("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .bodyValue(
                    mapOf(
                        "client_id" to githubOAuthProperties.clientId,
                        "client_secret" to githubOAuthProperties.clientSecret,
                        "code" to code,
                    ),
                ).retrieve()
                .bodyToMono<GithubTokenResponse>()
                .block()
                ?.accessToken ?: throw BusinessException(AuthError.InvalidToken)

        val userInfo =
            webClient
                .get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono<GithubUserInfo>()
                .block() ?: throw BusinessException(AuthError.InvalidCredentials)

        return userInfo
    }
}
