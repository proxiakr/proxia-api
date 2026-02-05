package kr.proxia.core.api.controller.v1.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import kr.proxia.core.domain.CreateProject

data class CreateProjectRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    @field:Pattern(
        regexp = "^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$",
        message = "Subdomain must start and end with a lowercase letter or digit, and can contain hyphens in between. Length must be between 1 and 63 characters.",
    )
    val subdomain: String,
) {
    fun toDomain() =
        CreateProject(
            name = name,
            subdomain = subdomain,
        )
}
