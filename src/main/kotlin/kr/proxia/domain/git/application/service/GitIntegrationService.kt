package kr.proxia.domain.git.application.service

import kr.proxia.domain.git.domain.entity.GitIntegrationEntity
import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.infra.client.GitIntegrationClient
import kr.proxia.domain.git.presentation.request.CreateGitIntegrationRequest
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class GitIntegrationService(
    private val gitIntegrationClient: GitIntegrationClient,
    private val gitIntegrationRepository: GitIntegrationRepository,
    private val securityHolder: SecurityHolder,
) {
    fun createGitIntegration(request: CreateGitIntegrationRequest) {
        val userId = securityHolder.getUserId()

        if (gitIntegrationRepository.existsByUserIdAndProvider(userId, request.provider)) {
            throw BusinessException(GitError.GIT_INTEGRATION_ALREADY_EXISTS)
        }

        val gitIntegration =
            when (request.provider) {
                GitIntegrationProvider.GITHUB -> {
                    val token = gitIntegrationClient.exchangeGithubCode(request.code)

                    GitIntegrationEntity(
                        userId = userId,
                        provider = GitIntegrationProvider.GITHUB,
                        accessToken = token.accessToken,
                        expiresAt = LocalDateTime.now().plusSeconds(token.expiresIn),
                    )
                }

                else -> throw BusinessException(GitError.UNSUPPORTED_GIT_PROVIDER)
            }

        gitIntegrationRepository.save(gitIntegration)
    }

    fun deleteGitIntegration(integrationId: UUID) {
        val userId = securityHolder.getUserId()
        val integration =
            gitIntegrationRepository.findById(integrationId).orElseThrow {
                BusinessException(GitError.GIT_INTEGRATION_NOT_FOUND)
            }

        if (integration.userId != userId) {
            throw BusinessException(GitError.GIT_INTEGRATION_ACCESS_DENIED)
        }

        if (integration.isDeleted) {
            throw BusinessException(GitError.GIT_INTEGRATION_NOT_FOUND)
        }

        integration.delete()
    }
}
