package kr.proxia.domain.auth.presentation.v1.request

data class GithubLoginRequest(
    val code: String
)