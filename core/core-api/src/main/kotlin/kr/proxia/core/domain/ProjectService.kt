package kr.proxia.core.domain

import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.storage.db.core.entity.Project
import kr.proxia.storage.db.core.repository.ProjectRepository
import kr.proxia.storage.db.core.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val workspaceRepository: WorkspaceRepository
) {
    fun getProjects(userId: UUID, workspaceId: UUID): List<Project> {
        val workspace = workspaceRepository.findByIdAndMember(workspaceId, userId)
            ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        return projectRepository.findAllByWorkspace(workspace)
    }

    fun getProject(userId: UUID, workspaceId: UUID, projectId: UUID): Project {
        val workspace = workspaceRepository.findByIdAndMember(workspaceId, userId)
            ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        return projectRepository.findByIdAndWorkspace(projectId, workspace)
            ?: throw CoreException(ErrorType.PROJECT_NOT_FOUND)
    }

    fun createProject(userId: UUID, workspaceId: UUID, createProject: CreateProject): Project {
        val workspace = workspaceRepository.findByIdAndMember(workspaceId, userId)
            ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        val project = Project(
            name = createProject.name,
            subdomain = createProject.subdomain,
            workspace = workspace
        )

        return projectRepository.save(project)
    }

    fun deleteProject(userId: UUID, workspaceId: UUID, projectId: UUID) {
        val workspace = workspaceRepository.findByIdAndMember(workspaceId, userId)
            ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        val project = projectRepository.findByIdAndWorkspace(projectId, workspace)
            ?: throw CoreException(ErrorType.PROJECT_NOT_FOUND)

        projectRepository.delete(project)
    }
}