package kr.proxia.domain.project.application.service

import kr.proxia.domain.project.domain.entity.ProjectEntity
import kr.proxia.domain.project.domain.error.ProjectError
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.project.presentation.request.CreateProjectRequest
import kr.proxia.domain.project.presentation.response.ProjectDetailResponse
import kr.proxia.domain.project.presentation.response.ProjectResponse
import kr.proxia.domain.user.domain.error.UserError
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.response.OffsetLimit
import kr.proxia.global.response.PageResponse
import kr.proxia.global.response.toResponse
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProjectService(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val securityHolder: SecurityHolder,
) {
    fun createProject(request: CreateProjectRequest) {
        val userId = securityHolder.getUserId()

        projectRepository.save(
            ProjectEntity(
                userId = userId,
                name = request.name,
            ),
        )
    }

    fun getProjects(offsetLimit: OffsetLimit): PageResponse<ProjectResponse> {
        val userId = securityHolder.getUserId()
        val projects = projectRepository.findAllByUserId(userId, offsetLimit.toPageable())

        return projects
            .map { project ->
                ProjectResponse(
                    id = project.id,
                    name = project.name,
                    createdAt = project.createdAt,
                    updatedAt = project.updatedAt,
                )
            }.toResponse()
    }

    fun getProject(projectId: Long): ProjectDetailResponse {
        val userId = securityHolder.getUserId()
        val user = userRepository.findByIdOrNull(userId) ?: throw BusinessException(UserError.USER_NOT_FOUND)

        val project =
            projectRepository.findByIdOrNull(projectId)
                ?: throw BusinessException(ProjectError.PROJECT_NOT_FOUND)

        if (project.userId != userId) {
            throw BusinessException(ProjectError.PROJECT_ACCESS_DENIED)
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
            createdAt = project.createdAt,
            updatedAt = project.updatedAt,
        )
    }

    fun deleteProject(projectId: Long) {
        val userId = securityHolder.getUserId()
        val project = projectRepository.findByIdOrNull(projectId) ?: throw BusinessException(ProjectError.PROJECT_NOT_FOUND)

        if (project.userId != userId) {
            throw BusinessException(ProjectError.PROJECT_ACCESS_DENIED)
        }

        if (project.isDeleted) {
            throw BusinessException(ProjectError.PROJECT_ALREADY_DELETED)
        }

        project.delete()
    }
}
