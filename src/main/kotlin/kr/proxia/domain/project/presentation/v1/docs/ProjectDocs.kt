package kr.proxia.domain.project.presentation.v1.docs

import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.project.presentation.v1.request.CreateProjectRequest
import kr.proxia.domain.project.presentation.v1.response.ProjectDetailResponse
import kr.proxia.domain.project.presentation.v1.response.ProjectResponse
import kr.proxia.global.response.PageResponse

@Tag(name = "Project")
interface ProjectDocs {
    fun createProject(request: CreateProjectRequest)

    fun getProjects(
        offset: Long,
        limit: Int,
    ): PageResponse<ProjectResponse>

    fun getProject(projectId: Long): ProjectDetailResponse

    fun deleteProject(projectId: Long)
}