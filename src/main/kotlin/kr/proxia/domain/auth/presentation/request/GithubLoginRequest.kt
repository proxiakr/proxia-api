package kr.proxia.domain.auth.presentation.request

data class GithubLoginRequest(
    val code: String,
)