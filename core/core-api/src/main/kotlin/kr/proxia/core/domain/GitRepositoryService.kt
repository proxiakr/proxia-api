package kr.proxia.core.domain

import kr.proxia.client.git.github.GitHubConnectionClient
import kr.proxia.client.git.github.GitHubRepositoryClient
import kr.proxia.core.support.OffsetLimit
import kr.proxia.core.support.Page
import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.storage.db.core.repository.GitConnectionRepository
import kr.proxia.storage.db.core.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class GitRepositoryService(
    private val workspaceRepository: WorkspaceRepository,
    private val gitConnectionRepository: GitConnectionRepository,
    private val githubConnectionClient: GitHubConnectionClient,
    private val githubRepositoryClient: GitHubRepositoryClient,
) {
    @Transactional
    fun getRepositories(
        userId: UUID,
        workspaceId: UUID,
        connectionId: UUID,
        offsetLimit: OffsetLimit,
    ): Page<GitRepository> {
        val workspace =
            workspaceRepository.findByIdAndMember(workspaceId, userId)
                ?: throw CoreException(ErrorType.WORKSPACE_NOT_FOUND)

        val connection =
            gitConnectionRepository.findByIdAndWorkspace(connectionId, workspace)
                ?: throw CoreException(ErrorType.GIT_CONNECTION_NOT_FOUND)

        val repositoriesResponse =
            githubRepositoryClient.getGithubRepositories(
                accessToken =
                    if (connection.accessToken == null ||
                        connection.expiresAt == null ||
                        connection.expiresAt!!.isBefore(LocalDateTime.now())
                    ) {
                        val token = githubConnectionClient.getInstallationToken(connection.installationId).token
                        connection.accessToken = token
                        connection.expiresAt = LocalDateTime.now().plusHours(1L)
                        token
                    } else {
                        connection.accessToken!!
                    },
                page = (offsetLimit.offset / offsetLimit.limit) + 1,
                perPage = offsetLimit.limit,
            )

        return Page(
            content =
                repositoriesResponse.repositories.map {
                    GitRepository(
                        name = it.name,
                    )
                },
            hasNext = repositoriesResponse.hasNext,
        )
    }
}
