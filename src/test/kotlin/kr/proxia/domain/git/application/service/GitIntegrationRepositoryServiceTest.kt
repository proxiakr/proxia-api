package kr.proxia.domain.git.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.proxia.domain.git.domain.entity.GitIntegrationEntity
import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.infra.client.GithubRepositoryClient
import kr.proxia.domain.git.infra.data.GithubRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.repository.findByIdOrNull

class GitIntegrationRepositoryServiceTest :
    BehaviorSpec({
        val gitIntegrationRepository = mockk<GitIntegrationRepository>()
        val githubRepositoryClient = mockk<GithubRepositoryClient>()
        val securityHolder = mockk<SecurityHolder>()

        val service =
            GitIntegrationRepositoryService(
                gitIntegrationRepository,
                githubRepositoryClient,
                securityHolder,
            )

        Given("getRepositories") {
            val userId = 1L
            val integrationId = 5L

            When("유효한 요청") {
                val integration =
                    mockk<GitIntegrationEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                        every { provider } returns GitIntegrationProvider.GITHUB
                        every { accessToken } returns "gho_test_token"
                        every { isDeleted } returns false
                    }
                val repositories =
                    listOf(
                        GithubRepository(
                            id = 1,
                            name = "test-repo",
                            fullName = "user/test-repo",
                            private = false,
                            url = "https://github.com/user/test-repo",
                        ),
                    )

                every { securityHolder.getUserId() } returns userId
                every { gitIntegrationRepository.findByIdOrNull(integrationId) } returns integration
                every { githubRepositoryClient.getGithubRepositories("gho_test_token") } returns repositories

                val result = service.getRepositories(integrationId)

                Then("repositories 반환") {
                    result.size shouldBe 1
                    result[0].name shouldBe "test-repo"
                }
            }

            When("integration이 존재하지 않음") {
                every { securityHolder.getUserId() } returns userId
                every { gitIntegrationRepository.findByIdOrNull(integrationId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            service.getRepositories(integrationId)
                        }
                    exception.error shouldBe GitError.GIT_INTEGRATION_NOT_FOUND
                }
            }

            When("다른 사용자의 integration") {
                val otherUserId = 2L
                val integration =
                    mockk<GitIntegrationEntity>(relaxed = true) {
                        every { this@mockk.userId } returns otherUserId
                    }

                every { securityHolder.getUserId() } returns userId
                every { gitIntegrationRepository.findByIdOrNull(integrationId) } returns integration

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            service.getRepositories(integrationId)
                        }
                    exception.error shouldBe GitError.GIT_INTEGRATION_ACCESS_DENIED
                }
            }

            When("삭제된 integration") {
                val integration =
                    mockk<GitIntegrationEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                        every { isDeleted } returns true
                    }

                every { securityHolder.getUserId() } returns userId
                every { gitIntegrationRepository.findByIdOrNull(integrationId) } returns integration

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            service.getRepositories(integrationId)
                        }
                    exception.error shouldBe GitError.GIT_INTEGRATION_NOT_FOUND
                }
            }
        }
    })
