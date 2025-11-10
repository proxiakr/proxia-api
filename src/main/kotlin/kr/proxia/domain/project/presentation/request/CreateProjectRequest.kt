package kr.proxia.domain.project.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateProjectRequest(
    @field:NotBlank(message = "프로젝트 이름은 필수입니다")
    @field:Size(min = 1, max = 100, message = "프로젝트 이름은 1-100자여야 합니다")
    val name: String,
    @field:NotBlank(message = "슬러그는 필수입니다")
    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "슬러그는 소문자, 숫자, 하이픈만 사용 가능합니다")
    @field:Size(min = 1, max = 50, message = "슬러그는 1-50자여야 합니다")
    val slug: String,
)
