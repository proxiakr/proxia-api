package kr.proxia.core.api.controller.v1.response

import kr.proxia.core.enums.GitProvider
import java.util.UUID

data class GitConnectionResponse(
    val id: UUID,
    val name: String,
    val provider: GitProvider,
)
