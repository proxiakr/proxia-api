package kr.proxia.domain.auth.presentation.v1.request

data class ReissueRequest(
    val refreshToken: String
)