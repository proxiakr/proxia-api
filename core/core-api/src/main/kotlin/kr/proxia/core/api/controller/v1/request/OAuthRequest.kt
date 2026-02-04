package kr.proxia.core.api.controller.v1.request

import jakarta.validation.constraints.NotBlank

data class OAuthRequest(
    @field:NotBlank
    val code: String,
)
