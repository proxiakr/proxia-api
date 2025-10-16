package kr.proxia.domain.auth.presentation.dto.response

data class ReissueResponse(
    val accessToken: String,
    val refreshToken: String
)