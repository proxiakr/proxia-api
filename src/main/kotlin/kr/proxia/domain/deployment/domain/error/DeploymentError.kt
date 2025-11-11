package kr.proxia.domain.deployment.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class DeploymentError(
    override val message: String,
    override val status: HttpStatus,
) : BaseError {
    SERVICE_NOT_FOUND("Service not found", HttpStatus.NOT_FOUND),
    APP_RESOURCE_NOT_FOUND("App resource not found", HttpStatus.NOT_FOUND),
    REPOSITORY_URL_NOT_FOUND("Repository URL not found", HttpStatus.BAD_REQUEST),
    DEPLOYMENT_FAILED("Deployment failed", HttpStatus.INTERNAL_SERVER_ERROR),
}
