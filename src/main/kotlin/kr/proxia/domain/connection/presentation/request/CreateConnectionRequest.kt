package kr.proxia.domain.connection.presentation.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.proxia.domain.connection.domain.enums.ConnectionType
import java.util.UUID

data class CreateConnectionRequest(
    @field:NotNull(message = "소스 서비스 ID는 필수입니다")
    val sourceId: UUID,
    @field:NotNull(message = "타겟 서비스 ID는 필수입니다")
    val targetId: UUID,
    @field:NotNull(message = "연결 타입은 필수입니다")
    val type: ConnectionType,
    @field:Size(max = 100, message = "라벨은 최대 100자까지 가능합니다")
    val label: String?,
    val weight: Int?,
)
