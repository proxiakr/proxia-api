package kr.proxia.domain.deployment.domain.error

import kr.proxia.domain.service.domain.enums.ServiceType
import kr.proxia.global.error.DomainError
import org.springframework.http.HttpStatus

sealed class DeploymentError(
    override val status: HttpStatus,
    override val message: String,
) : DomainError {
    data class InvalidResourceDeployment(
        val resource: ServiceType,
    ) : DeploymentError(
        HttpStatus.BAD_REQUEST,
        "Attempted to deploy an incorrect or invalid resource: ${resource.name.lowercase()}",
    )
}
