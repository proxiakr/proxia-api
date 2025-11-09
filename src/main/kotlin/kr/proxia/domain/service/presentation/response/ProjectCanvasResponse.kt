package kr.proxia.domain.service.presentation.response

import kr.proxia.domain.connection.presentation.response.ConnectionResponse

data class ProjectCanvasResponse(
    val projectId: Long,
    val services: List<ServiceResponse>,
    val connections: List<ConnectionResponse>,
)
