package kr.proxia.domain.connection.presentation.request

import kr.proxia.domain.connection.domain.enums.ConnectionType

data class UpdateConnectionRequest(
    val type: ConnectionType,
    val label: String?,
)
