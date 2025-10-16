package kr.proxia.global.security.oauth2.google.client

import kr.proxia.global.security.oauth2.google.data.GoogleUserInfo
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class GoogleOAuthClient(private val webClient: WebClient) {
    fun getUserInfo(idToken: String): GoogleUserInfo {
        val userInfo = webClient.get()
            .uri("https://oauth2.googleapis.com/tokeninfo?id_token=$idToken")
            .retrieve()
            .bodyToMono<GoogleUserInfo>()
            .block() ?: throw IllegalArgumentException("Invalid ID token")

        return userInfo
    }
}