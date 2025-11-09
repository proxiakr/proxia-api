package kr.proxia.domain.git.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class GitError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    GIT_INTEGRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Git integration not found"),
    GIT_INTEGRATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have access to this git integration"),
    GIT_INTEGRATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "Git integration already exists for this provider"),
    UNSUPPORTED_GIT_PROVIDER(HttpStatus.BAD_REQUEST, "Unsupported git provider"),
    GITHUB_API_ERROR(HttpStatus.BAD_GATEWAY, "Failed to communicate with GitHub API"),
    INVALID_GITHUB_CODE(HttpStatus.BAD_REQUEST, "Invalid GitHub authorization code"),
}
