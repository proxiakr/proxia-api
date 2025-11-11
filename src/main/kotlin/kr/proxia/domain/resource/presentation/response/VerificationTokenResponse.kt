package kr.proxia.domain.resource.presentation.response

data class VerificationTokenResponse(
    val token: String,
    val txtRecord: String,
    val verified: Boolean,
)
