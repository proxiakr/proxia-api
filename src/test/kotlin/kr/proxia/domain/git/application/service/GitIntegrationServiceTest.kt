package kr.proxia.domain.git.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kr.proxia.domain.git.domain.entity.GitIntegrationEntity
import kr.proxia.domain.git.domain.enums.GitIntegrationProvider
import kr.proxia.domain.git.domain.error.GitError
import kr.proxia.domain.git.domain.repository.GitIntegrationRepository
import kr.proxia.domain.git.infra.client.GitIntegrationClient
import kr.proxia.domain.git.infra.data.ExchangeGithubCodeResponse
import kr.proxia.domain.git.presentation.request.CreateGitIntegrationRequest
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import java.util.Optional
import java.util.UUID

class GitIntegrationServiceTest :
    BehaviorSpec({
        val gitIntegrationClient = mockk<GitIntegrationClient>()
        val gitIntegrationRepository = mockk<GitIntegrationRepository>()
        val securityHolder = mockk<SecurityHolder>()

        val gitIntegrationService =
            GitIntegrationService(
                gitIntegrationClient,
                gitIntegrationRepository,
                securityHolder,
            )

        Given("createGitIntegration") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val request =
                CreateGitIntegrationRequest(
                    provider = GitIntegrationProvider.GITHUB,
                    code = "github_code_123",
                )

            When("유효한 요청") {
                val tokenResponse =
                    ExchangeGithubCodeResponse(
                        accessToken = "gho_test_token",
                        tokenType = "bearer",
                        scope = "repo",
                        expiresIn = 28800,
                    )

                every { securityHolder.getUserId() } returns userId
                every {
                    gitIntegrationRepository.existsByUserIdAndProvider(userId, GitIntegrationProvider.GITHUB)
                } returns false
                every { gitIntegrationClient.exchangeGithubCode(request.code) } returns tokenResponse
                every { gitIntegrationRepository.save(any()) } returns mockk(relaxed = true)

                gitIntegrationService.createGitIntegration(request)

                Then("integration 생성") {
                    val slot = slot<GitIntegrationEntity>()
                    verify { gitIntegrationRepository.save(capture(slot)) }
                    slot.captured.userId shouldBe userId
                    slot.captured.provider shouldBe GitIntegrationProvider.GITHUB
                    slot.captured.accessToken shouldBe tokenResponse.accessToken
                }
            }

            When("이미 존재하는 integration") {
                every { securityHolder.getUserId() } returns userId
                every {
                    gitIntegrationRepository.existsByUserIdAndProvider(userId, GitIntegrationProvider.GITHUB)
                } returns true

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            gitIntegrationService.createGitIntegration(request)
                        }
                    exception.error shouldBe GitError.AlreadyExists
                }
            }
        }

        Given("deleteGitIntegration") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val integrationId = UUID.fromString("40000000-0000-0000-0000-000000000001")

            When("유효한 요청") {
                val integration =
                    mockk<GitIntegrationEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                        every { isDeleted } returns false
                    }

                every { securityHolder.getUserId() } returns userId
                every { gitIntegrationRepository.findById(integrationId) } returns Optional.of(integration)

                gitIntegrationService.deleteGitIntegration(integrationId)

                Then("integration 삭제") {
                    verify { integration.delete() }
                }
            }

            When("integration이 존재하지 않음") {
                every { securityHolder.getUserId() } returns userId
                every { gitIntegrationRepository.findById(integrationId) } returns Optional.empty()

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            gitIntegrationService.deleteGitIntegration(integrationId)
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
                every { gitIntegrationRepository.findById(integrationId) } returns Optional.of(integration)

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            gitIntegrationService.deleteGitIntegration(integrationId)
                        }
                    exception.error shouldBe GitError.AccessDenied
                }
            }
        }
    })
