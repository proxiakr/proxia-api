package kr.proxia.domain.connection.application.service

import kr.proxia.domain.connection.domain.entity.ConnectionEntity
import kr.proxia.domain.connection.domain.error.ConnectionError
import kr.proxia.domain.connection.domain.repository.ConnectionRepository
import kr.proxia.domain.connection.presentation.request.CreateConnectionRequest
import kr.proxia.domain.connection.presentation.request.UpdateConnectionRequest
import kr.proxia.domain.connection.presentation.response.ConnectionResponse
import kr.proxia.domain.project.domain.error.ProjectError
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.service.domain.error.ServiceError
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ConnectionService(
    private val connectionRepository: ConnectionRepository,
    private val serviceRepository: ServiceRepository,
    private val projectRepository: ProjectRepository,
    private val securityHolder: SecurityHolder,
) {
    fun createConnection(
        projectId: UUID,
        request: CreateConnectionRequest,
    ) {
        val userId = securityHolder.getUserId()
        validateProjectAccess(projectId, userId)

        if (request.sourceId == request.targetId) {
            throw BusinessException(ConnectionError.INVALID_CONNECTION)
        }

        val sourceService =
            serviceRepository.findByIdAndDeletedAtIsNull(request.sourceId)
                ?: throw BusinessException(ConnectionError.SOURCE_SERVICE_NOT_FOUND)

        val targetService =
            serviceRepository.findByIdAndDeletedAtIsNull(request.targetId)
                ?: throw BusinessException(ConnectionError.TARGET_SERVICE_NOT_FOUND)

        if (sourceService.projectId != projectId || targetService.projectId != projectId) {
            throw BusinessException(ServiceError.SERVICE_ACCESS_DENIED)
        }

        if (connectionRepository.existsBySourceIdAndTargetIdAndDeletedAtIsNull(request.sourceId, request.targetId)) {
            throw BusinessException(ConnectionError.CONNECTION_ALREADY_EXISTS)
        }

        connectionRepository.save(
            ConnectionEntity(
                projectId = projectId,
                sourceId = request.sourceId,
                targetId = request.targetId,
                type = request.type,
                label = request.label,
                weight = request.weight,
            ),
        )
    }

    fun getConnections(projectId: UUID): List<ConnectionResponse> {
        val userId = securityHolder.getUserId()
        validateProjectAccess(projectId, userId)

        return connectionRepository
            .findAllByProjectIdAndDeletedAtIsNull(projectId)
            .map { ConnectionResponse.from(it) }
    }

    fun getConnection(connectionId: UUID): ConnectionResponse {
        val userId = securityHolder.getUserId()
        val connection =
            connectionRepository.findByIdAndDeletedAtIsNull(connectionId)
                ?: throw BusinessException(ConnectionError.CONNECTION_NOT_FOUND)

        validateProjectAccess(connection.projectId, userId)

        return ConnectionResponse.from(connection)
    }

    @Transactional
    fun updateConnection(
        connectionId: UUID,
        request: UpdateConnectionRequest,
    ) {
        val userId = securityHolder.getUserId()
        val connection =
            connectionRepository.findByIdAndDeletedAtIsNull(connectionId)
                ?: throw BusinessException(ConnectionError.CONNECTION_NOT_FOUND)

        validateProjectAccess(connection.projectId, userId)

        connection.update(
            type = request.type,
            label = request.label,
            weight = request.weight,
        )
    }

    @Transactional
    fun deleteConnection(connectionId: UUID) {
        val userId = securityHolder.getUserId()
        val connection =
            connectionRepository.findByIdAndDeletedAtIsNull(connectionId)
                ?: throw BusinessException(ConnectionError.CONNECTION_NOT_FOUND)

        validateProjectAccess(connection.projectId, userId)

        connection.delete()
    }

    private fun validateProjectAccess(
        projectId: UUID,
        userId: UUID,
    ) {
        val project = projectRepository.findByIdAndDeletedAtIsNull(projectId) ?: throw BusinessException(ProjectError.PROJECT_NOT_FOUND)

        if (project.userId != userId) {
            throw BusinessException(ProjectError.PROJECT_ACCESS_DENIED)
        }
    }
}
