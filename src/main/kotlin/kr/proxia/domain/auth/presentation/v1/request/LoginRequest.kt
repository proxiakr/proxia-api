package kr.proxia.domain.auth.presentation.v1.request

data class LoginRequest(
    val email: String,
    val password: String
)