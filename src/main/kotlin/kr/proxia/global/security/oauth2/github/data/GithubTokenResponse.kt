package kr.proxia.global.security.oauth2.github.data

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubTokenResponse(
    @field:JsonProperty("access_token")
    val accessToken: String,
)