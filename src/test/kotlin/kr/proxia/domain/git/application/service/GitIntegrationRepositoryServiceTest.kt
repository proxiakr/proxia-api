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
import kr.proxia.domain.git.infra.client.GitRepositoryClient
import kr.proxia.domain.git.infra.data.GitRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class GitIntegrationRepositoryServiceTest :
    BehaviorSpec({
        val gitIntegrationRepository = mockk<GitIntegrationRepository>()
        val gitRepositoryClient = mockk<GitRepositoryClient>()
        val securityHolder = mockk<SecurityHolder>()

        val service =
            GitIntegrationRepositoryService(
                gitIntegrationRepository,
                gitRepositoryClient,
                securityHolder,
            )

        Given("getRepositories") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val integrationId = UUID.fromString("40000000-0000-0000-0000-000000000001")

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
                        GitRepository(
                            id = 1,
                            name = "test-repo",
                            fullName = "user/test-repo",
                            private = false,
                            url = "https://github.com/user/test-repo",
                        ),
                    )

                every { securityHolder.getUserId() } returns userId
                every { gitIntegrationRepository.findByIdOrNull(integrationId) } returns integration
                every { gitRepositoryClient.getGitRepositories("gho_test_token") } returns repositories

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
                    exception.error shouldBe GitError.NotFound
                }
            }

            When("다른 사용자의 integration") {
                val otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002")
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
                    exception.error shouldBe GitError.AccessDenied
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
                    exception.error shouldBe GitError.NotFound
                }
            }
        }
    })
