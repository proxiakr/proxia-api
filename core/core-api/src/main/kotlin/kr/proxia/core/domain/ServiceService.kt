package kr.proxia.core.domain

import kr.proxia.client.git.github.GitHubConnectionClient
import kr.proxia.client.git.github.GitHubRepositoryClient
import kr.proxia.core.enums.ServiceStatus
import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.storage.db.core.entity.AppService
import kr.proxia.storage.db.core.entity.DatabaseService
import kr.proxia.storage.db.core.repository.GitConnectionRepository
import kr.proxia.storage.db.core.repository.ProjectRepository
import kr.proxia.storage.db.core.repository.ServiceRepository
import kr.proxia.storage.db.core.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kr.proxia.storage.db.core.entity.Service as ServiceEntity

@Service
class ServiceService(
    private val workspaceRepository: WorkspaceRepository,
    private val projectRepository: ProjectRepository,
    private val serviceRepository: ServiceRepository,
    private val gitConnectionRepository: GitConnectionRepository,
    private val githubConnectionClient: GitHubConnectionClient,
    private val githubRepositoryClient: GitHubRepositoryClient,
) {
    fun getServices(
        userId: UUID,
        workspaceId: UUID,
        projectId: UUID,
    ): List<ServiceEntity> {
        val workspace =
            workspaceRepository.findByIdAndMember(workspaceId, userId)
                ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        val project =
            projectRepository.findByIdAndWorkspace(projectId, workspace)
                ?: throw CoreException(ErrorType.PROJECT_NOT_FOUND)

        return serviceRepository.findAllByProject(project)
    }

    fun getService(
        userId: UUID,
        workspaceId: UUID,
        projectId: UUID,
        serviceId: UUID,
    ): ServiceEntity {
        val workspace =
            workspaceRepository.findByIdAndMember(workspaceId, userId)
                ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        val project =
            projectRepository.findByIdAndWorkspace(projectId, workspace)
                ?: throw CoreException(ErrorType.PROJECT_NOT_FOUND)

        return serviceRepository.findByIdAndProject(serviceId, project)
            ?: throw CoreException(ErrorType.SERVICE_NOT_FOUND)
    }

    @Transactional
    fun createAppService(
        userId: UUID,
        workspaceId: UUID,
        projectId: UUID,
        createAppService: CreateAppService,
    ): ServiceEntity {
        val workspace =
            workspaceRepository.findByIdAndMember(workspaceId, userId)
                ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        val project =
            projectRepository.findByIdAndWorkspace(projectId, workspace)
                ?: throw CoreException(ErrorType.PROJECT_NOT_FOUND)

        val gitConnection =
            gitConnectionRepository.findByIdAndWorkspace(createAppService.gitConnectionId, workspace)
                ?: throw CoreException(ErrorType.GIT_CONNECTION_NOT_FOUND)

        val tokenResponse = githubConnectionClient.getInstallationToken(gitConnection.installationId)

        val hasAccess =
            githubRepositoryClient.verifyRepositoryAccess(
                tokenResponse.token,
                createAppService.repoFullName,
            )

        if (!hasAccess) {
            throw CoreException(ErrorType.GIT_REPOSITORY_NOT_FOUND)
        }

        val service =
            AppService(
                name = createAppService.name,
                x = createAppService.x,
                y = createAppService.y,
                project = project,
                status = ServiceStatus.STARTING,
                repoFullName = createAppService.repoFullName,
                branch = createAppService.branch ?: "main",
                port = createAppService.framework.defaultPort,
                framework = createAppService.framework,
                rootDirectory = ".",
                buildCommand = createAppService.buildCommand,
                startCommand = createAppService.startCommand,
                gitConnection = gitConnection,
            )

        return serviceRepository.save(service)
    }

    @Transactional
    fun createDatabaseService(
        userId: UUID,
        workspaceId: UUID,
        projectId: UUID,
        createDatabaseService: CreateDatabaseService,
    ): ServiceEntity {
        val workspace =
            workspaceRepository.findByIdAndMember(workspaceId, userId)
                ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        val project =
            projectRepository.findByIdAndWorkspace(projectId, workspace)
                ?: throw CoreException(ErrorType.PROJECT_NOT_FOUND)

        val engine = createDatabaseService.engine

        val service =
            DatabaseService(
                name = createDatabaseService.name,
                x = createDatabaseService.x,
                y = createDatabaseService.y,
                project = project,
                status = ServiceStatus.STARTING,
                engine = engine,
                version = DatabaseEngineConfig.of(engine).supportedVersions.last(),
                database = project.name.replace("-", "_"),
                username = DatabaseCredentialGenerator.generateUsername(engine),
                password = DatabaseCredentialGenerator.generatePassword(),
            )

        return serviceRepository.save(service)
    }

    @Transactional
    fun deleteService(
        userId: UUID,
        workspaceId: UUID,
        projectId: UUID,
        serviceId: UUID,
    ) {
        val workspace =
            workspaceRepository.findByIdAndMember(workspaceId, userId)
                ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        val project =
            projectRepository.findByIdAndWorkspace(projectId, workspace)
                ?: throw CoreException(ErrorType.PROJECT_NOT_FOUND)

        val service =
            serviceRepository.findByIdAndProject(serviceId, project)
                ?: throw CoreException(ErrorType.SERVICE_NOT_FOUND)

        serviceRepository.delete(service)
    }
}
