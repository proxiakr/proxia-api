package kr.proxia.domain.project.application.service

import kr.proxia.domain.connection.domain.repository.ConnectionRepository
import kr.proxia.domain.connection.presentation.response.ConnectionResponse
import kr.proxia.domain.project.domain.entity.ProjectEntity
import kr.proxia.domain.project.domain.error.ProjectError
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.project.presentation.request.CreateProjectRequest
import kr.proxia.domain.project.presentation.response.ProjectDetailResponse
import kr.proxia.domain.project.presentation.response.ProjectResponse
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.domain.service.presentation.response.ProjectCanvasResponse
import kr.proxia.domain.service.presentation.response.ServiceResponse
import kr.proxia.domain.user.domain.error.UserError
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import kr.proxia.global.support.OffsetLimit
import kr.proxia.global.support.PageResponse
import kr.proxia.global.support.toResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProjectService(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val serviceRepository: ServiceRepository,
    private val connectionRepository: ConnectionRepository,
    private val securityHolder: SecurityHolder,
) {
    @Transactional
    fun createProject(request: CreateProjectRequest) {
        val userId = securityHolder.getUserId()
        val slug = request.slug

        if (projectRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            throw BusinessException(ProjectError.SlugAlreadyExists)
        }

        projectRepository.save(
            ProjectEntity(
                userId = userId,
                name = request.name,
                slug = slug,
            ),
        )
    }

    fun getProjects(offsetLimit: OffsetLimit): PageResponse<ProjectResponse> {
        val userId = securityHolder.getUserId()
        val projects = projectRepository.findAllByUserIdAndDeletedAtIsNull(userId, offsetLimit.toPageable())

        return projects
            .map { project ->
                ProjectResponse(
                    id = project.id,
                    name = project.name,
                    slug = project.slug,
                    createdAt = project.createdAt,
                    updatedAt = project.updatedAt,
                )
            }.toResponse()
    }

    fun getProject(projectId: UUID): ProjectDetailResponse {
        val userId = securityHolder.getUserId()
        val user = userRepository.findByIdOrNull(userId) ?: throw BusinessException(UserError.NotFound)

        val project =
            projectRepository.findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectError.NotFound)

        if (project.userId != userId) {
            throw BusinessException(ProjectError.AccessDenied)
        }

        return ProjectDetailResponse(
            id = project.id,
            user =
                ProjectDetailResponse.User(
                    id = user.id,
                    name = user.name,
                    avatarUrl = user.avatarUrl,
                ),
            name = project.name,
            slug = project.slug,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt,
        )
    }

    @Transactional
    fun deleteProject(projectId: UUID) {
        val userId = securityHolder.getUserId()
        val project =
            projectRepository.findByIdAndDeletedAtIsNull(projectId) ?: throw BusinessException(ProjectError.NotFound)

        if (project.userId != userId) {
            throw BusinessException(ProjectError.AccessDenied)
        }

        serviceRepository
            .findAllByProjectIdAndDeletedAtIsNull(projectId)
            .forEach { service ->
                connectionRepository
                    .findAllBySourceIdOrTargetIdAndDeletedAtIsNull(service.id, service.id)
                    .forEach { it.delete() }
                service.delete()
            }

        connectionRepository
            .findAllByProjectIdAndDeletedAtIsNull(projectId)
            .forEach { it.delete() }

        project.delete()
    }

    fun getProjectCanvas(projectId: UUID): ProjectCanvasResponse {
        val userId = securityHolder.getUserId()
        val project = projectRepository.findByIdAndDeletedAtIsNull(projectId) ?: throw BusinessException(ProjectError.NotFound)

        if (project.userId != userId) {
            throw BusinessException(ProjectError.AccessDenied)
        }

        val services =
            serviceRepository
                .findAllByProjectIdAndDeletedAtIsNull(projectId)
                .map { ServiceResponse.of(it) }

        val connections =
            connectionRepository
                .findAllByProjectIdAndDeletedAtIsNull(projectId)
                .map { ConnectionResponse.from(it) }

        return ProjectCanvasResponse(
            projectId = projectId,
            services = services,
            connections = connections,
        )
    }
}
