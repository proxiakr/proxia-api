package kr.proxia.domain.project.presentation.v1.docs

import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.project.presentation.v1.request.CreateProjectRequest
import kr.proxia.domain.project.presentation.v1.response.ProjectDetailResponse
import kr.proxia.domain.project.presentation.v1.response.ProjectResponse

@Tag(name = "Project")
interface ProjectDocs {
    fun createProject(request: CreateProjectRequest)

    fun getProjects(): List<ProjectResponse>

    fun getProject(projectId: Long): ProjectDetailResponse

    fun deleteProject(projectId: Long)
}