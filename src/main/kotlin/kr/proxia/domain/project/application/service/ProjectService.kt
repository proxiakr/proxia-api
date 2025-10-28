package kr.proxia.domain.project.application.service

import kr.proxia.domain.project.domain.entity.ProjectEntity
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.project.presentation.v1.request.CreateProjectRequest
import kr.proxia.domain.project.presentation.v1.response.ProjectDetailResponse
import kr.proxia.domain.project.presentation.v1.response.ProjectResponse
import kr.proxia.domain.user.domain.repository.UserRepository
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
        val slug = request.slug

        if (projectRepository.existsBySlug(slug))
            throw IllegalArgumentException("Slug already exists")

        projectRepository.save(
            ProjectEntity(
                userId = userId,
                name = request.name,
                slug = slug,
            )
        )
    }

    fun getProjects(offsetLimit: OffsetLimit): PageResponse<ProjectResponse> {
        val userId = securityHolder.getUserId()
        val projects = projectRepository.findAllByUserId(userId, offsetLimit.toPageable())

        return projects.map { project ->
            ProjectResponse(
                id = project.id,
                name = project.name,
                slug = project.slug,
                createdAt = project.createdAt,
                updatedAt = project.updatedAt,
            )
        }.toResponse()
    }

    fun getProject(projectId: Long): ProjectDetailResponse {
        val userId = securityHolder.getUserId()
        val user = userRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("User not found")

        val project = projectRepository.findByIdOrNull(projectId)
            ?: throw IllegalArgumentException("Project does not exist")

        if (project.userId != userId)
            throw IllegalArgumentException("Project does not belong to user")

        return ProjectDetailResponse(
            id = project.id,
            user = ProjectDetailResponse.User(
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

    fun deleteProject(projectId: Long) {
        val userId = securityHolder.getUserId()
        val project = projectRepository.findByIdOrNull(projectId) ?: throw IllegalArgumentException("Project not found")

        if (project.userId != userId)
            throw IllegalArgumentException("No permissions for project")

        if (project.isDeleted)
            throw IllegalArgumentException("Project already deleted")

        project.delete()
    }
}