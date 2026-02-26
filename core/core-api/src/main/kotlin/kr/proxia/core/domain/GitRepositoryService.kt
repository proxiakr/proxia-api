package kr.proxia.core.domain

import kr.proxia.client.git.github.GitHubConnectionClient
import kr.proxia.client.git.github.GitHubRepositoryClient
import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.storage.db.core.repository.GitConnectionRepository
import kr.proxia.storage.db.core.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GitRepositoryService(
    private val workspaceRepository: WorkspaceRepository,
    private val gitConnectionRepository: GitConnectionRepository,
    private val githubConnectionClient: GitHubConnectionClient,
    private val githubRepositoryClient: GitHubRepositoryClient,
) {
    fun getRepositories(
        userId: UUID,
        workspaceId: UUID,
        connectionId: UUID,
    ): List<GitRepository> {
        val workspace = workspaceRepository.findByIdAndMember(workspaceId, userId)
            ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        val connection = gitConnectionRepository.findByIdAndWorkspace(connectionId, workspace)
            ?: throw CoreException(ErrorType.GIT_CONNECTION_NOT_FOUND)

        val tokenResponse = githubConnectionClient.getInstallationToken(connection.installationId)

        return githubRepositoryClient.getGithubRepositories(tokenResponse.token).map {
            GitRepository(name = it.fullName)
        }
    }
}
