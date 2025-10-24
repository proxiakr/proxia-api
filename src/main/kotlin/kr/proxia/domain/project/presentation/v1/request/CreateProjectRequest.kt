package kr.proxia.domain.project.presentation.v1.request

data class CreateProjectRequest(
    val name: String,
    val slug: String,
)