package kr.proxia.domain.auth.presentation.request

data class LoginRequest(
    val email: String,
    val password: String,
)