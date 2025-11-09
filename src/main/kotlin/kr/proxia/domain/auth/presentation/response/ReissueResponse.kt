package kr.proxia.domain.auth.presentation.response

data class ReissueResponse(
    val accessToken: String,
    val refreshToken: String,
)
