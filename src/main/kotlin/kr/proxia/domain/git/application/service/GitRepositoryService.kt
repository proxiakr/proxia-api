package kr.proxia.domain.git.application.service

import kr.proxia.domain.git.domain.entity.GitRepositoryEntity
import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.domain.repository.GitRepositoryRepository
import kr.proxia.domain.git.presentation.request.CreateGitRepositoryRequest
import kr.proxia.domain.git.presentation.response.GitRepositoryResponse
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GitRepositoryService(
    private val gitRepositoryRepository: GitRepositoryRepository,
    private val gitIntegrationRepository: GitIntegrationRepository,
    private val securityHolder: SecurityHolder,
) {
    @Transactional
    fun createGitRepository(
        integrationId: UUID,
        request: CreateGitRepositoryRequest,
    ) {
        val userId = securityHolder.getUserId()
        val gitIntegration =
            gitIntegrationRepository.findByIdAndDeletedAtIsNull(integrationId)
                ?: throw BusinessException(GitError.GIT_INTEGRATION_NOT_FOUND)

        if (gitIntegration.userId != userId) {
            throw BusinessException(GitError.GIT_INTEGRATION_ACCESS_DENIED)
        }

        if (gitRepositoryRepository.findByFullNameAndDeletedAtIsNull(request.fullName) != null) {
            throw BusinessException(GitError.GIT_REPOSITORY_ALREADY_EXISTS)
        }

        gitRepositoryRepository.save(
            GitRepositoryEntity(
                gitIntegrationId = integrationId,
                fullName = request.fullName,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getGitRepositories(integrationId: UUID): List<GitRepositoryResponse> {
        val userId = securityHolder.getUserId()
        val gitIntegration =
            gitIntegrationRepository.findByIdAndDeletedAtIsNull(integrationId)
                ?: throw BusinessException(GitError.GIT_INTEGRATION_NOT_FOUND)

        if (gitIntegration.userId != userId) {
            throw BusinessException(GitError.GIT_INTEGRATION_ACCESS_DENIED)
        }

        return GitRepositoryResponse.of(
            gitRepositoryRepository.findAllByGitIntegrationIdAndDeletedAtIsNull(integrationId),
        )
    }

    @Transactional
    fun deleteGitRepository(repositoryId: UUID) {
        val userId = securityHolder.getUserId()
        val gitRepository =
            gitRepositoryRepository.findByIdAndDeletedAtIsNull(repositoryId)
                ?: throw BusinessException(GitError.GIT_REPOSITORY_NOT_FOUND)

        val gitIntegration =
            gitIntegrationRepository.findByIdAndDeletedAtIsNull(gitRepository.gitIntegrationId)
                ?: throw BusinessException(GitError.GIT_INTEGRATION_NOT_FOUND)

        if (gitIntegration.userId != userId) {
            throw BusinessException(GitError.GIT_REPOSITORY_ACCESS_DENIED)
        }

        gitRepository.delete()
    }
}
