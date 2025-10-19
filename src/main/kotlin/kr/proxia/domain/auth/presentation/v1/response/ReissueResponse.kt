package kr.proxia.domain.auth.presentation.v1.response

data class ReissueResponse(
    val accessToken: String,
    val refreshToken: String
)