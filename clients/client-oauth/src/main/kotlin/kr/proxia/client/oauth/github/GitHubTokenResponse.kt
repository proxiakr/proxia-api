package kr.proxia.client.oauth.github

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubTokenResponse(
    @field:JsonProperty("access_token")
    val accessToken: String,
    @field:JsonProperty("token_type")
    val tokenType: String,
    val scope: String,
)
