package kr.proxia.domain.auth.presentation.v1.request

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)