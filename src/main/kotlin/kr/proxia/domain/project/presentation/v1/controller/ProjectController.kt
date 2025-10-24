package kr.proxia.domain.project.presentation.v1.controller

import kr.proxia.domain.project.application.service.ProjectService
import kr.proxia.domain.project.presentation.v1.docs.ProjectDocs
import kr.proxia.domain.project.presentation.v1.request.CreateProjectRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects")
class ProjectController(private val projectService: ProjectService) : ProjectDocs {
    @PostMapping
    override fun createProject(@RequestBody request: CreateProjectRequest) = projectService.createProject(request)

    @GetMapping
    override fun getProjects() = projectService.getProjects()

    @GetMapping("/{projectId}")
    override fun getProject(@PathVariable projectId: Long) = projectService.getProject(projectId)

    @DeleteMapping("/{projectId}")
    override fun deleteProject(@PathVariable projectId: Long) = projectService.deleteProject(projectId)
}