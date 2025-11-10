package kr.proxia.domain.connection.presentation.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.proxia.domain.connection.domain.enums.ConnectionType

data class CreateConnectionRequest(
    @field:NotNull(message = "소스 서비스 ID는 필수입니다")
    @field:Positive(message = "소스 서비스 ID는 양수여야 합니다")
    val sourceId: Long,
    @field:NotNull(message = "타겟 서비스 ID는 필수입니다")
    @field:Positive(message = "타겟 서비스 ID는 양수여야 합니다")
    val targetId: Long,
    @field:NotNull(message = "연결 타입은 필수입니다")
    val type: ConnectionType,
    @field:Size(max = 100, message = "라벨은 최대 100자까지 가능합니다")
    val label: String?,
    val weight: Int?,
)
