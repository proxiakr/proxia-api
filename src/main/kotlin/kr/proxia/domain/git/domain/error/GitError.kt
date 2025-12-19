package kr.proxia.domain.git.domain.error

import kr.proxia.global.error.DomainError
import org.springframework.http.HttpStatus

sealed class GitError(
    override val status: HttpStatus,
    override val message: String,
) : DomainError {
    data object NotFound : GitError(
        HttpStatus.NOT_FOUND,
        "Git integration not found"
    )
    data object AccessDenied : GitError(
        HttpStatus.FORBIDDEN,
        "You do not have access to this git integration"
    )
    data object AlreadyExists : GitError(
        HttpStatus.CONFLICT,
        "Git integration already exists for this provider"
    )
    data object UnsupportedProvider : GitError(
        HttpStatus.BAD_REQUEST,
        "Unsupported git provider"
    )
    data object GithubApiError : GitError(
        HttpStatus.BAD_GATEWAY,
        "Failed to communicate with GitHub API"
    )
    data object InvalidGithubCode : GitError(
        HttpStatus.BAD_REQUEST,
        "Invalid GitHub authorization code"
    )
}
