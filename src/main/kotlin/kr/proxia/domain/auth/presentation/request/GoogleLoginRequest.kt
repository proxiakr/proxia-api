package kr.proxia.domain.auth.presentation.request

data class GoogleLoginRequest(
    val idToken: String,
)