package kr.proxia.domain.project.presentation.v1.controller

import kr.proxia.domain.project.application.service.ProjectService
import kr.proxia.domain.project.presentation.v1.docs.ProjectDocs
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects")
class ProjectController(private val projectService: ProjectService) : ProjectDocs {
}