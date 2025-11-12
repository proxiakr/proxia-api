package kr.proxia.domain.auth.presentation.response

import java.util.UUID

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
) {
    data class User(
        val id: UUID,
        val email: String,
        val name: String,
        val avatarUrl: String?,
    )

    companion object
}
