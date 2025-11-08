package kr.proxia.domain.git.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class GitError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    GIT_INTEGRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Git integration not found"),
    GIT_INTEGRATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have access to this git integration"),
    UNSUPPORTED_GIT_PROVIDER(HttpStatus.BAD_REQUEST, "Unsupported git provider"),
}
