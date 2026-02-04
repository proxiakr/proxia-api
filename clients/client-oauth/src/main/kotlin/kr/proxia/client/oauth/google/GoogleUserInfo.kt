package kr.proxia.client.oauth.google

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleUserInfo(
    val id: String,
    val email: String,
    @field:JsonProperty("verified_email")
    val verifiedEmail: Boolean?,
    val name: String?,
    val picture: String?,
)
