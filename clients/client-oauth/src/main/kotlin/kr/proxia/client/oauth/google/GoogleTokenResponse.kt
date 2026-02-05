package kr.proxia.client.oauth.google

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleTokenResponse(
    @field:JsonProperty("access_token")
    val accessToken: String,
    @field:JsonProperty("expires_in")
    val expiresIn: Int,
    @field:JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @field:JsonProperty("token_type")
    val tokenType: String,
    val scope: String,
)
