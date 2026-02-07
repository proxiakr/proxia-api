package kr.proxia.core.api.controller.v1.request

import jakarta.validation.constraints.NotBlank
import kr.proxia.core.domain.CreateWorkspace

data class CreateWorkspaceRequest(
    @field:NotBlank
    val name: String,
) {
    fun toDomain() =
        CreateWorkspace(
            name = name,
        )
}
