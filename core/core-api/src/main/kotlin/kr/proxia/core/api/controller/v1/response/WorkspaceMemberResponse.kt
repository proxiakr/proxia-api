package kr.proxia.core.api.controller.v1.response

import kr.proxia.core.enums.WorkspaceMemberRole
import java.util.UUID

data class WorkspaceMemberResponse(
    val user: User,
    val role: WorkspaceMemberRole,
) {
    data class User(
        val id: UUID,
        val email: String,
    )
}
