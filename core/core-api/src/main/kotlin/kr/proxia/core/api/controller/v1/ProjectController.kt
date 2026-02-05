package kr.proxia.core.api.controller.v1

import kr.proxia.core.api.controller.v1.request.CreateProjectRequest
import kr.proxia.core.api.controller.v1.response.ProjectDetailResponse
import kr.proxia.core.api.controller.v1.response.ProjectResponse
import kr.proxia.core.domain.ProjectService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/projects")
class ProjectController(
    private val projectService: ProjectService
) {
    @GetMapping
    fun getProjects(@AuthenticationPrincipal userId: UUID, @PathVariable workspaceId: UUID): List<ProjectResponse> {
        val projects = projectService.getProjects(userId, workspaceId)

        return projects.map {
            ProjectResponse(
                id = it.id,
                name = it.name,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }

    @GetMapping("/{projectId}")
    fun getProject(@AuthenticationPrincipal userId: UUID, @PathVariable workspaceId: UUID, @PathVariable projectId: UUID): ProjectDetailResponse {
        val project = projectService.getProject(userId, workspaceId, projectId)

        return ProjectDetailResponse(
            id = project.id,
            name = project.name,
            subdomain = project.subdomain,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt,
        )
    }

    @PostMapping
    fun createProject(@AuthenticationPrincipal userId: UUID, @PathVariable workspaceId: UUID, @RequestBody request: CreateProjectRequest): ProjectResponse {
        val project = projectService.createProject(userId, workspaceId, request.toDomain())

        return ProjectResponse(
            id = project.id,
            name = project.name,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt,
        )
    }

    @DeleteMapping("/{projectId}")
    fun deleteProject(@AuthenticationPrincipal userId: UUID, @PathVariable workspaceId: UUID, @PathVariable projectId: UUID) {
        projectService.deleteProject(userId, workspaceId, projectId)
    }
}