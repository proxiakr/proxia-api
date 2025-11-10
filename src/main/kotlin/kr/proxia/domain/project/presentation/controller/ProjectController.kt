package kr.proxia.domain.project.presentation.controller

import kr.proxia.domain.project.application.service.ProjectService
import kr.proxia.domain.project.presentation.docs.ProjectDocs
import kr.proxia.domain.project.presentation.request.CreateProjectRequest
import kr.proxia.global.response.OffsetLimit
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/projects")
class ProjectController(
    private val projectService: ProjectService,
) : ProjectDocs {
    @PostMapping
    override fun createProject(
        @RequestBody request: CreateProjectRequest,
    ) = projectService.createProject(request)

    @GetMapping
    override fun getProjects(
        @RequestParam offset: Long,
        @RequestParam limit: Int,
    ) = projectService.getProjects(OffsetLimit(offset, limit))

    @GetMapping("/{projectId}")
    override fun getProject(
        @PathVariable projectId: Long,
    ) = projectService.getProject(projectId)

    @DeleteMapping("/{projectId}")
    override fun deleteProject(
        @PathVariable projectId: Long,
    ) = projectService.deleteProject(projectId)

    @GetMapping("/{projectId}/canvas")
    override fun getProjectCanvas(
        @PathVariable projectId: Long,
    ) = projectService.getProjectCanvas(projectId)
}
