package kr.proxia.client.oauth.github

import kr.proxia.client.oauth.OAuthProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

@Component
class GitHubOAuthClient(
    private val githubRestClient: RestClient,
    private val githubApiRestClient: RestClient,
    private val properties: OAuthProperties,
) {
    fun getAccessToken(code: String): GitHubTokenResponse {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("code", code)
            add("client_id", properties.github.clientId)
            add("client_secret", properties.github.clientSecret)
            add("redirect_uri", properties.github.redirectUri)
        }

        return githubRestClient.post()
            .uri("/login/oauth/access_token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(params)
            .retrieve()
            .body(GitHubTokenResponse::class.java)!!
    }

    fun getUserInfo(accessToken: String): GitHubUserInfo {
        return githubApiRestClient.get()
            .uri("/user")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(GitHubUserInfo::class.java)!!
    }

    fun getUserEmails(accessToken: String): List<GitHubEmail> {
        return githubApiRestClient.get()
            .uri("/user/emails")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(object : ParameterizedTypeReference<List<GitHubEmail>>() {})!!
    }
}
