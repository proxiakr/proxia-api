package kr.proxia.core.domain.auth

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)
