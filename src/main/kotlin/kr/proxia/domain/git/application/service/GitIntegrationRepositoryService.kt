package kr.proxia.domain.git.application.service

import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.infra.client.GithubRepositoryClient
import kr.proxia.domain.git.presentation.response.GitIntegrationRepositoryResponse
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class GitIntegrationRepositoryService(
    private val gitIntegrationRepository: GitIntegrationRepository,
    private val githubRepositoryClient: GithubRepositoryClient,
    private val securityHolder: SecurityHolder,
) {
    fun getRepositories(gitIntegrationId: Long): List<GitIntegrationRepositoryResponse> {
        val userId = securityHolder.getUserId()
        val gitIntegration =
            gitIntegrationRepository.findByIdOrNull(gitIntegrationId)
                ?: throw BusinessException(GitError.GIT_INTEGRATION_NOT_FOUND)

        if (gitIntegration.userId != userId) {
            throw BusinessException(GitError.GIT_INTEGRATION_ACCESS_DENIED)
        }

        val repositories =
            when (gitIntegration.provider) {
                GitIntegrationProvider.GITHUB -> {
                    val repositories = githubRepositoryClient.getGithubRepositories(gitIntegration.accessToken)

                    GitIntegrationRepositoryResponse.of(repositories)
                }

                else -> throw BusinessException(GitError.UNSUPPORTED_GIT_PROVIDER)
            }

        return repositories
    }
}
