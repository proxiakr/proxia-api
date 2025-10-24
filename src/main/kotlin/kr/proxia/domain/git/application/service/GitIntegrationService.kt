package kr.proxia.domain.git.application.service

import kr.proxia.domain.git.domain.entity.GitIntegrationEntity
import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.infra.client.GitIntegrationClient
import kr.proxia.domain.git.presentation.v1.request.CreateGitIntegrationRequest
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

@Service
class GitIntegrationService(
    private val gitIntegrationClient: GitIntegrationClient,
    private val securityHolder: SecurityHolder,
    private val gitIntegrationRepository: GitIntegrationRepository
) {
    fun createGitIntegration(request: CreateGitIntegrationRequest) {
        val userId = securityHolder.getUserId()
        val gitIntegration = when (request.provider) {
            GitIntegrationProvider.GITHUB -> {
                val token = gitIntegrationClient.exchangeGithubCode(request.code)

                GitIntegrationEntity(
                    userId = userId,
                    provider = GitIntegrationProvider.GITHUB,
                    accessToken = token.accessToken,
                    expiresAt = LocalDateTime.now().plusSeconds(token.expiresIn)
                )
            }

            else -> throw IllegalArgumentException("Unsupported provider")
        }

        gitIntegrationRepository.save(gitIntegration)
    }
}