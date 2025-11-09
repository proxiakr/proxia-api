package kr.proxia.domain.service.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.service.presentation.response.ProjectCanvasResponse

@Tag(name = "Project Canvas")
interface ProjectCanvasDocs {
    @Operation(summary = "Get Project Canvas")
    fun getProjectCanvas(projectId: Long): ProjectCanvasResponse
}
