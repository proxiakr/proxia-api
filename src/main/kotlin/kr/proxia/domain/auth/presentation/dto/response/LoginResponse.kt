package kr.proxia.domain.auth.presentation.dto.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
) {
    data class User(
        val id: Long,
        val email: String,
        val name: String,
        val avatarUrl: String?
    )
}