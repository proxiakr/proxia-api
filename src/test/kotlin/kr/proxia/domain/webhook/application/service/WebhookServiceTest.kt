package kr.proxia.domain.webhook.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kr.proxia.domain.deployment.application.service.DeploymentService
import kr.proxia.domain.resource.domain.entity.AppResourceEntity
import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.service.domain.entity.ServiceEntity
import kr.proxia.domain.service.domain.enums.ServiceType
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.domain.webhook.domain.entity.WebhookEventEntity
import kr.proxia.domain.webhook.domain.repository.WebhookEventRepository
import java.util.Optional

class WebhookServiceTest :
    BehaviorSpec({
        val webhookEventRepository = mockk<WebhookEventRepository>()
        val serviceRepository = mockk<ServiceRepository>()
        val appResourceRepository = mockk<AppResourceRepository>()
        val deploymentService = mockk<DeploymentService>()
        val objectMapper = ObjectMapper()

        val webhookService =
            WebhookService(
                webhookEventRepository,
                serviceRepository,
                appResourceRepository,
                deploymentService,
                objectMapper,
            )

        Given("handleGithubPushEvent") {
            val event = "push"
            val repositoryUrl = "https://github.com/test/repo.git"
            val branch = "main"
            val deliveryId = "test-delivery-id"

            val payload =
                mapOf(
                    "ref" to "refs/heads/$branch",
                    "repository" to
                        mapOf(
                            "clone_url" to repositoryUrl,
                        ),
                )

            When("레포지토리 URL이 없는 경우") {
                val emptyPayload = mapOf<String, Any>()

                webhookService.handleGithubPushEvent(event, emptyPayload, deliveryId)

                Then("아무 동작도 하지 않음") {
                    verify(exactly = 0) { deploymentService.deploy(any(), any()) }
                }
            }

            When("매칭되는 서비스가 없는 경우") {
                val webhookEventSlot = slot<WebhookEventEntity>()

                every { serviceRepository.findAll() } returns emptyList()
                every { webhookEventRepository.save(capture(webhookEventSlot)) } returns mockk(relaxed = true)

                webhookService.handleGithubPushEvent(event, payload, deliveryId)

                Then("웹훅 이벤트 저장") {
                    webhookEventSlot.captured.event shouldBe event
                    webhookEventSlot.captured.deliveryId shouldBe deliveryId
                    webhookEventSlot.captured.success shouldBe true
                    webhookEventSlot.captured.errorMessage shouldBe "No matching services found"
                }
            }

            When("매칭되는 서비스가 있는 경우") {
                val serviceId = 1L
                val appResourceId = 1L

                val service = mockk<ServiceEntity>(relaxed = true)
                every { service.id } returns serviceId
                every { service.type } returns ServiceType.APP
                every { service.targetId } returns appResourceId

                val appResource = mockk<AppResourceEntity>(relaxed = true)
                every { appResource.repositoryUrl } returns repositoryUrl
                every { appResource.branch } returns branch

                val webhookEventSlot = slot<WebhookEventEntity>()

                every { serviceRepository.findAll() } returns listOf(service)
                every { appResourceRepository.findById(appResourceId) } returns Optional.of(appResource)
                every { webhookEventRepository.save(capture(webhookEventSlot)) } returns mockk(relaxed = true)
                every { deploymentService.deploy(serviceId, branch) } just Runs

                webhookService.handleGithubPushEvent(event, payload, deliveryId)

                Then("배포 트리거") {
                    verify { deploymentService.deploy(serviceId, branch) }
                    webhookEventSlot.captured.serviceId shouldBe serviceId
                    webhookEventSlot.captured.success shouldBe true
                }
            }

            When("배포 트리거 실패") {
                val serviceId = 1L
                val appResourceId = 1L
                val errorMessage = "Deployment failed"

                val service = mockk<ServiceEntity>(relaxed = true)
                every { service.id } returns serviceId
                every { service.type } returns ServiceType.APP
                every { service.targetId } returns appResourceId

                val appResource = mockk<AppResourceEntity>(relaxed = true)
                every { appResource.repositoryUrl } returns repositoryUrl
                every { appResource.branch } returns branch

                val webhookEvent = mockk<WebhookEventEntity>(relaxed = true)
                every { webhookEvent.markAsFailed(any()) } just Runs

                every { serviceRepository.findAll() } returns listOf(service)
                every { appResourceRepository.findById(appResourceId) } returns Optional.of(appResource)
                every { webhookEventRepository.save(any()) } returns webhookEvent
                every { deploymentService.deploy(serviceId, branch) } throws RuntimeException(errorMessage)

                webhookService.handleGithubPushEvent(event, payload, deliveryId)

                Then("실패 이벤트 저장") {
                    verify { webhookEvent.markAsFailed(errorMessage) }
                }
            }

            When("브랜치가 null인 경우에도 매칭") {
                val serviceId = 1L
                val appResourceId = 1L

                val payloadWithoutRef = mapOf<String, Any>("repository" to mapOf("clone_url" to repositoryUrl))

                val service = mockk<ServiceEntity>(relaxed = true)
                every { service.id } returns serviceId
                every { service.type } returns ServiceType.APP
                every { service.targetId } returns appResourceId

                val appResource = mockk<AppResourceEntity>(relaxed = true)
                every { appResource.repositoryUrl } returns repositoryUrl
                every { appResource.branch } returns null

                every { serviceRepository.findAll() } returns listOf(service)
                every { appResourceRepository.findById(appResourceId) } returns Optional.of(appResource)
                every { webhookEventRepository.save(any()) } returns mockk(relaxed = true)
                every { deploymentService.deploy(serviceId, null) } just Runs

                webhookService.handleGithubPushEvent(event, payloadWithoutRef, deliveryId)

                Then("배포 트리거 (브랜치 null)") {
                    verify { deploymentService.deploy(serviceId, null) }
                }
            }

            When("HTTPS와 SSH URL 모두 정규화되어 매칭") {
                val serviceId = 1L
                val appResourceId = 1L

                val httpsUrl = "https://github.com/test/repo.git"
                val sshUrl = "git@github.com:test/repo.git"

                val service = mockk<ServiceEntity>(relaxed = true)
                every { service.id } returns serviceId
                every { service.type } returns ServiceType.APP
                every { service.targetId } returns appResourceId

                val appResource = mockk<AppResourceEntity>(relaxed = true)
                every { appResource.repositoryUrl } returns sshUrl
                every { appResource.branch } returns branch

                val httpsPayload =
                    mapOf(
                        "ref" to "refs/heads/$branch",
                        "repository" to mapOf("clone_url" to httpsUrl),
                    )

                every { serviceRepository.findAll() } returns listOf(service)
                every { appResourceRepository.findById(appResourceId) } returns Optional.of(appResource)
                every { webhookEventRepository.save(any()) } returns mockk(relaxed = true)
                every { deploymentService.deploy(serviceId, branch) } just Runs

                webhookService.handleGithubPushEvent(event, httpsPayload, deliveryId)

                Then("정규화 후 매칭되어 배포 트리거") {
                    verify { deploymentService.deploy(serviceId, branch) }
                }
            }
        }
    })
