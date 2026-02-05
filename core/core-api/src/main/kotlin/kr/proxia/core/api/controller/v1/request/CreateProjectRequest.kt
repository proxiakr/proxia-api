package kr.proxia.core.api.controller.v1.request

import kr.proxia.core.domain.CreateProject

data class CreateProjectRequest(
    val name: String,
    val subdomain: String
) {
    fun toDomain() = CreateProject(
        name = name,
        subdomain = subdomain
    )
}