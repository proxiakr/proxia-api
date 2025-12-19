package kr.proxia.domain.project.domain.error

import kr.proxia.global.error.DomainError
import org.springframework.http.HttpStatus

sealed class ProjectError(
    override val status: HttpStatus,
    override val message: String,
) : DomainError {
    data object NotFound : ProjectError(
        HttpStatus.NOT_FOUND,
        "Project not found"
    )
    data object SlugAlreadyExists : ProjectError(
        HttpStatus.CONFLICT,
        "Slug already exists"
    )
    data object AccessDenied : ProjectError(
        HttpStatus.FORBIDDEN,
        "Project does not belong to user"
    )
}
