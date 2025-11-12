package kr.proxia.domain.deployment.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.proxia.domain.container.domain.repository.ContainerRepository
import kr.proxia.domain.deployment.domain.error.DeploymentError
import kr.proxia.domain.deployment.domain.repository.DeploymentRepository
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.resource.application.service.DomainService
import kr.proxia.domain.resource.domain.entity.AppResourceEntity
import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.resource.domain.repository.DomainResourceRepository
import kr.proxia.domain.service.domain.entity.ServiceEntity
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.global.docker.service.DockerService
import kr.proxia.global.error.BusinessException
import kr.proxia.global.reverseproxy.service.ReverseProxyService
import kr.proxia.global.security.encryption.EncryptionService
import java.util.Optional

class DeploymentServiceTest :
    BehaviorSpec({
        val deploymentRepository = mockk<DeploymentRepository>()
        val serviceRepository = mockk<ServiceRepository>()
        val projectRepository = mockk<ProjectRepository>()
        val appResourceRepository = mockk<AppResourceRepository>()
        val containerRepository = mockk<ContainerRepository>()
        val domainResourceRepository = mockk<DomainResourceRepository>()
        val dockerService = mockk<DockerService>(relaxed = true)
        val reverseProxyService = mockk<ReverseProxyService>(relaxed = true)
        val domainService = mockk<DomainService>(relaxed = true)
        val encryptionService = mockk<EncryptionService>(relaxed = true)

        // Create network을 호출하는 init 블록을 처리하기 위해 relaxed mock 사용
        every { dockerService.createNetwork() } returns Unit

        val deploymentService =
            DeploymentService(
                deploymentRepository,
                serviceRepository,
                projectRepository,
                appResourceRepository,
                containerRepository,
                domainResourceRepository,
                dockerService,
                reverseProxyService,
                domainService,
                encryptionService,
            )

        Given("deploy") {
            val serviceId = 1L
            val branch = "main"

            When("서비스를 찾을 수 없는 경우") {
                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            deploymentService.deploy(serviceId, branch)
                        }
                    exception.error shouldBe DeploymentError.SERVICE_NOT_FOUND
                }
            }

            When("AppResource를 찾을 수 없는 경우") {
                val service = mockk<ServiceEntity>(relaxed = true)
                every { service.targetId } returns 1L

                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns service
                every { appResourceRepository.findById(1L) } returns Optional.empty()

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            deploymentService.deploy(serviceId, branch)
                        }
                    exception.error shouldBe DeploymentError.APP_RESOURCE_NOT_FOUND
                }
            }

            When("레포지토리 URL이 없는 경우") {
                val service = mockk<ServiceEntity>(relaxed = true)
                every { service.targetId } returns 1L

                val appResource = mockk<AppResourceEntity>(relaxed = true)
                every { appResource.repositoryUrl } returns null

                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns service
                every { appResourceRepository.findById(1L) } returns Optional.of(appResource)

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            deploymentService.deploy(serviceId, branch)
                        }
                    exception.error shouldBe DeploymentError.REPOSITORY_URL_NOT_FOUND
                }
            }

            When("targetId가 null인 경우") {
                val service = mockk<ServiceEntity>(relaxed = true)
                every { service.targetId } returns null

                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns service

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            deploymentService.deploy(serviceId, branch)
                        }
                    exception.error shouldBe DeploymentError.APP_RESOURCE_NOT_FOUND
                }
            }
        }
    })
