package kr.proxia.domain.project.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class ProjectError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Project not found"),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Project does not belong to user"),
    PROJECT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "Project already deleted"),
}
