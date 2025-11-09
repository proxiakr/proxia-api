package kr.proxia.domain.connection.presentation.request

import kr.proxia.domain.connection.domain.enums.ConnectionType

data class CreateConnectionRequest(
    val sourceId: Long,
    val targetId: Long,
    val type: ConnectionType,
    val label: String?,
)
