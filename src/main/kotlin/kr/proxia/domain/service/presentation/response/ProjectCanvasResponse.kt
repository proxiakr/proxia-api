package kr.proxia.domain.service.presentation.response

import kr.proxia.domain.connection.presentation.response.ConnectionResponse
import java.util.UUID

data class ProjectCanvasResponse(
    val projectId: UUID,
    val services: List<ServiceResponse>,
    val connections: List<ConnectionResponse>,
)
