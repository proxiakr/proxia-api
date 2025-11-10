package kr.proxia.domain.service.application.service

import kr.proxia.domain.connection.domain.repository.ConnectionRepository
import kr.proxia.domain.project.domain.error.ProjectError
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.service.domain.entity.ServiceEntity
import kr.proxia.domain.service.domain.error.ServiceError
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.domain.service.presentation.request.CreateServiceRequest
import kr.proxia.domain.service.presentation.request.UpdateServicePositionRequest
import kr.proxia.domain.service.presentation.request.UpdateServiceRequest
import kr.proxia.domain.service.presentation.response.ServiceResponse
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val connectionRepository: ConnectionRepository,
    private val projectRepository: ProjectRepository,
    private val securityHolder: SecurityHolder,
) {
    fun createService(
        projectId: Long,
        request: CreateServiceRequest,
    ) {
        val userId = securityHolder.getUserId()
        validateProjectAccess(projectId, userId)

        if (serviceRepository.existsByProjectIdAndNameAndDeletedAtIsNull(projectId, request.name)) {
            throw BusinessException(ServiceError.SERVICE_NAME_ALREADY_EXISTS)
        }

        serviceRepository.save(
            ServiceEntity(
                projectId = projectId,
                userId = userId,
                name = request.name,
                description = request.description,
                type = request.type,
                x = request.x,
                y = request.y,
                width = request.width,
                height = request.height,
            ),
        )
    }

    fun getServices(projectId: Long): List<ServiceResponse> {
        val userId = securityHolder.getUserId()
        validateProjectAccess(projectId, userId)

        return serviceRepository
            .findAllByProjectIdAndDeletedAtIsNull(projectId)
            .map { ServiceResponse.from(it) }
    }

    fun getService(serviceId: Long): ServiceResponse {
        val userId = securityHolder.getUserId()
        val service = serviceRepository.findByIdOrNull(serviceId) ?: throw BusinessException(ServiceError.SERVICE_NOT_FOUND)

        if (service.userId != userId) {
            throw BusinessException(ServiceError.SERVICE_ACCESS_DENIED)
        }

        return ServiceResponse.from(service)
    }

    @Transactional
    fun updateService(
        serviceId: Long,
        request: UpdateServiceRequest,
    ) {
        val userId = securityHolder.getUserId()
        val service = serviceRepository.findByIdOrNull(serviceId) ?: throw BusinessException(ServiceError.SERVICE_NOT_FOUND)

        if (service.userId != userId) {
            throw BusinessException(ServiceError.SERVICE_ACCESS_DENIED)
        }

        if (service.name != request.name &&
            serviceRepository.existsByProjectIdAndNameAndDeletedAtIsNull(service.projectId, request.name)
        ) {
            throw BusinessException(ServiceError.SERVICE_NAME_ALREADY_EXISTS)
        }

        service.update(
            name = request.name,
            description = request.description,
            type = request.type,
        )
    }

    @Transactional
    fun updateServicePosition(
        serviceId: Long,
        request: UpdateServicePositionRequest,
    ) {
        val userId = securityHolder.getUserId()
        val service = serviceRepository.findByIdOrNull(serviceId) ?: throw BusinessException(ServiceError.SERVICE_NOT_FOUND)

        if (service.userId != userId) {
            throw BusinessException(ServiceError.SERVICE_ACCESS_DENIED)
        }

        service.updatePosition(
            x = request.x,
            y = request.y,
            width = request.width,
            height = request.height,
        )
    }

    @Transactional
    fun deleteService(serviceId: Long) {
        val userId = securityHolder.getUserId()
        val service = serviceRepository.findByIdOrNull(serviceId) ?: throw BusinessException(ServiceError.SERVICE_NOT_FOUND)

        if (service.userId != userId) {
            throw BusinessException(ServiceError.SERVICE_ACCESS_DENIED)
        }

        if (service.isDeleted) {
            throw BusinessException(ServiceError.SERVICE_ALREADY_DELETED)
        }

        connectionRepository
            .findAllBySourceIdOrTargetIdAndDeletedAtIsNull(serviceId, serviceId)
            .forEach { it.delete() }

        service.delete()
    }

    private fun validateProjectAccess(
        projectId: Long,
        userId: Long,
    ) {
        val project = projectRepository.findByIdOrNull(projectId) ?: throw BusinessException(ProjectError.PROJECT_NOT_FOUND)

        if (project.userId != userId) {
            throw BusinessException(ProjectError.PROJECT_ACCESS_DENIED)
        }
    }
}
