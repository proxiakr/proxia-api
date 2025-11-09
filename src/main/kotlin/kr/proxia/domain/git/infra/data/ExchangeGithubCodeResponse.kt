package kr.proxia.domain.git.infra.data

import com.fasterxml.jackson.annotation.JsonProperty

data class ExchangeGithubCodeResponse(
    @field:JsonProperty("access_token")
    val accessToken: String,
    @field:JsonProperty("expires_in")
    val expiresIn: Long,
    val scope: String,
    @field:JsonProperty("token_type")
    val tokenType: String,
)
