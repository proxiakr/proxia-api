package kr.proxia.domain.git.application.service

import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.infra.client.GitRepositoryClient
import kr.proxia.domain.git.presentation.response.GitIntegrationRepositoryResponse
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GitIntegrationRepositoryService(
    private val gitIntegrationRepository: GitIntegrationRepository,
    private val gitRepositoryClient: GitRepositoryClient,
    private val securityHolder: SecurityHolder,
) {
    fun getRepositories(gitIntegrationId: UUID): List<GitIntegrationRepositoryResponse> {
        val userId = securityHolder.getUserId()
        val gitIntegration =
            gitIntegrationRepository.findByIdOrNull(gitIntegrationId)
                ?: throw BusinessException(GitError.NotFound)

        if (gitIntegration.userId != userId) {
            throw BusinessException(GitError.AccessDenied)
        }

        if (gitIntegration.isDeleted) {
            throw BusinessException(GitError.NotFound)
        }

        return when (gitIntegration.provider) {
            GitIntegrationProvider.GITHUB -> {
                val repositories = gitRepositoryClient.getGitRepositories(gitIntegration.accessToken)
                GitIntegrationRepositoryResponse.of(repositories)
            }

            else -> throw BusinessException(GitError.UnsupportedProvider)
        }
    }
}
