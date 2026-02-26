package kr.proxia.core.domain

import kr.proxia.client.git.github.GitHubConnectionClient
import kr.proxia.core.enums.GitProvider
import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.storage.db.core.entity.GitConnection
import kr.proxia.storage.db.core.repository.GitConnectionRepository
import kr.proxia.storage.db.core.repository.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GitConnectionService(
    private val workspaceRepository: WorkspaceRepository,
    private val gitConnectionRepository: GitConnectionRepository,
    private val githubConnectionClient: GitHubConnectionClient,
) {
    fun getConnections(
        userId: UUID,
        workspaceId: UUID,
    ): List<GitConnection> {
        val workspace = workspaceRepository.findByIdAndMember(workspaceId, userId)
            ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        return gitConnectionRepository.findAllByWorkspace(workspace)
    }

    fun createConnection(
        userId: UUID,
        workspaceId: UUID,
        createGitConnection: CreateGitConnection,
    ) {
        val workspace = workspaceRepository.findByIdAndMember(workspaceId, userId)
            ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        if (gitConnectionRepository.existsByWorkspaceAndInstallationId(workspace, createGitConnection.installationId)) {
            throw CoreException(ErrorType.GIT_CONNECTION_ALREADY_EXISTS)
        }

        val connection =
            when (createGitConnection.provider) {
                GitProvider.GITHUB -> {
                    val response = githubConnectionClient.getInstallationToken(createGitConnection.installationId)

                    GitConnection(
                        workspace = workspace,
                        name = createGitConnection.name,
                        provider = GitProvider.GITHUB,
                        installationId = createGitConnection.installationId,
                        accessToken = response.token,
                        expiresAt = response.expiresAt,
                    )
                }

                else -> throw CoreException(ErrorType.UNSUPPORTED_GIT_PROVIDER)
            }

        gitConnectionRepository.save(connection)
    }

    fun deleteConnection(
        userId: UUID,
        workspaceId: UUID,
        connectionId: UUID,
    ) {
        if (!workspaceRepository.existsByIdAndMember(workspaceId, userId)) {
            throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)
        }

        val connection = gitConnectionRepository.findByIdOrNull(connectionId)
            ?: throw CoreException(ErrorType.GIT_CONNECTION_NOT_FOUND)

        if (connection.workspace.id != workspaceId) {
            throw CoreException(ErrorType.ACCESS_DENIED)
        }

        gitConnectionRepository.delete(connection)
    }
}
