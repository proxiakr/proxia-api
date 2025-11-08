package kr.proxia.domain.auth.presentation.request

data class ReissueRequest(
    val refreshToken: String,
)