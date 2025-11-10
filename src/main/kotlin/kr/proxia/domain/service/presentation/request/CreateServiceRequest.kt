package kr.proxia.domain.service.presentation.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.proxia.domain.resource.presentation.request.CreateAppResourceRequest
import kr.proxia.domain.resource.presentation.request.CreateDatabaseResourceRequest
import kr.proxia.domain.service.domain.enums.ServiceType

data class CreateServiceRequest(
    @field:NotBlank(message = "서비스 이름은 필수입니다")
    @field:Size(min = 1, max = 100, message = "서비스 이름은 1-100자여야 합니다")
    val name: String,
    @field:Size(max = 500, message = "설명은 최대 500자까지 가능합니다")
    val description: String?,
    @field:NotNull(message = "서비스 타입은 필수입니다")
    val type: ServiceType,
    @field:NotNull(message = "X 좌표는 필수입니다")
    val x: Double,
    @field:NotNull(message = "Y 좌표는 필수입니다")
    val y: Double,
    @field:Valid
    val appResource: CreateAppResourceRequest?,
    @field:Valid
    val databaseResource: CreateDatabaseResourceRequest?,
)
