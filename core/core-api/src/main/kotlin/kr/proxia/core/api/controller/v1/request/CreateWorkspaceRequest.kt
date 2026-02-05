package kr.proxia.core.api.controller.v1.request

import kr.proxia.core.domain.CreateWorkspace

data class CreateWorkspaceRequest(
    val name: String,
) {
    fun toDomain() =
        CreateWorkspace(
            name = name,
        )
}
