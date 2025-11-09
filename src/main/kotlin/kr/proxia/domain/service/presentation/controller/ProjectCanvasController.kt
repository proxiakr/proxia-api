package kr.proxia.domain.service.presentation.controller

import kr.proxia.domain.connection.application.service.ConnectionService
import kr.proxia.domain.service.presentation.docs.ProjectCanvasDocs
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/projects/{projectId}")
class ProjectCanvasController(
    private val connectionService: ConnectionService,
) : ProjectCanvasDocs {
    @GetMapping("/canvas")
    override fun getProjectCanvas(
        @PathVariable projectId: Long,
    ) = connectionService.getProjectCanvas(projectId)
}
