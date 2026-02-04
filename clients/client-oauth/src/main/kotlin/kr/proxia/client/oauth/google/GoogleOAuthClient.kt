package kr.proxia.client.oauth.google

import kr.proxia.client.oauth.OAuthProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

@Component
class GoogleOAuthClient(
    private val googleRestClient: RestClient,
    private val googleApiRestClient: RestClient,
    private val properties: OAuthProperties,
) {
    fun getAccessToken(code: String): GoogleTokenResponse {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("code", code)
            add("client_id", properties.google.clientId)
            add("client_secret", properties.google.clientSecret)
            add("redirect_uri", properties.google.redirectUri)
            add("grant_type", "authorization_code")
        }

        return googleRestClient.post()
            .uri("/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(params)
            .retrieve()
            .body(GoogleTokenResponse::class.java)!!
    }

    fun getUserInfo(accessToken: String): GoogleUserInfo {
        return googleApiRestClient.get()
            .uri("/oauth2/v2/userinfo")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(GoogleUserInfo::class.java)!!
    }
}
