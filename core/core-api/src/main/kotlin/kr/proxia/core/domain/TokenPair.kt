package kr.proxia.core.domain

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)
