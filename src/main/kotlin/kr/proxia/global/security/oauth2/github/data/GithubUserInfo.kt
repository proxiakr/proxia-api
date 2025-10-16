package kr.proxia.global.security.oauth2.github.data

import com.fasterxml.jackson.annotation.JsonProperty

data class GithubUserInfo(
    val id: Long,
    val login: String,
    val name: String?,
    val email: String,
    @field:JsonProperty("avatar_url")
    val avatarUrl: String?
)