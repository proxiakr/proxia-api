package kr.proxia.domain.project.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.project.presentation.request.CreateProjectRequest
import kr.proxia.domain.project.presentation.response.ProjectDetailResponse
import kr.proxia.domain.project.presentation.response.ProjectResponse
import kr.proxia.global.support.PageResponse
import java.util.UUID

@Tag(name = "Project")
interface ProjectDocs {
    @Operation(summary = "Create Project")
    fun createProject(request: CreateProjectRequest)

    @Operation(summary = "Get Projects")
    fun getProjects(
        offset: Long,
        limit: Int,
    ): PageResponse<ProjectResponse>

    @Operation(summary = "Get Project")
    fun getProject(projectId: UUID): ProjectDetailResponse

    @Operation(summary = "Delete Project")
    fun deleteProject(projectId: UUID)

    @Operation(summary = "Get Project Canvas")
    fun getProjectCanvas(projectId: UUID): kr.proxia.domain.service.presentation.response.ProjectCanvasResponse
}
