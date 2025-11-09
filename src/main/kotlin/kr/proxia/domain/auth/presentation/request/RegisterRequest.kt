package kr.proxia.domain.auth.presentation.request

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
)
