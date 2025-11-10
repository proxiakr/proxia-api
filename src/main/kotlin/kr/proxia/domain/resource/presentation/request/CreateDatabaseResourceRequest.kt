package kr.proxia.domain.resource.presentation.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.proxia.domain.service.domain.enums.DatabaseType

data class CreateDatabaseResourceRequest(
    @field:NotNull(message = "데이터베이스 타입은 필수입니다")
    val type: DatabaseType,
    @field:Size(max = 100, message = "데이터베이스 이름은 최대 100자까지 가능합니다")
    val database: String?,
    @field:Size(max = 100, message = "사용자명은 최대 100자까지 가능합니다")
    val username: String?,
    @field:Size(max = 500, message = "비밀번호는 최대 500자까지 가능합니다")
    val password: String?,
)
