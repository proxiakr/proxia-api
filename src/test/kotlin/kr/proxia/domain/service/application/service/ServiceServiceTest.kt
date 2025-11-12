package kr.proxia.domain.service.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kr.proxia.domain.connection.domain.repository.ConnectionRepository
import kr.proxia.domain.project.domain.entity.ProjectEntity
import kr.proxia.domain.project.domain.error.ProjectError
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.resource.domain.repository.DatabaseResourceRepository
import kr.proxia.domain.resource.domain.repository.DomainResourceRepository
import kr.proxia.global.security.encryption.EncryptionService
import kr.proxia.domain.service.domain.entity.ServiceEntity
import kr.proxia.domain.service.domain.enums.ServiceType
import kr.proxia.domain.service.domain.error.ServiceError
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.domain.service.presentation.request.CreateServiceRequest
import kr.proxia.domain.service.presentation.request.UpdateServicePositionRequest
import kr.proxia.domain.service.presentation.request.UpdateServiceRequest
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class ServiceServiceTest :
    BehaviorSpec({
        val serviceRepository = mockk<ServiceRepository>()
        val connectionRepository = mockk<ConnectionRepository>()
        val projectRepository = mockk<ProjectRepository>()
        val appResourceRepository = mockk<AppResourceRepository>()
        val databaseResourceRepository = mockk<DatabaseResourceRepository>()
        val securityHolder = mockk<SecurityHolder>()

        val domainResourceRepository = mockk<DomainResourceRepository>()
        val encryptionService = mockk<EncryptionService>()

        val serviceService =
            ServiceService(
                serviceRepository,
                connectionRepository,
                projectRepository,
                appResourceRepository,
                databaseResourceRepository,
                domainResourceRepository,
                encryptionService,
                securityHolder,
            )

        Given("createService") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val projectId = UUID.fromString("10000000-0000-0000-0000-000000000001")
            val request =
                CreateServiceRequest(
                    name = "API Server",
                    description = "Main API Server",
                    type = ServiceType.APP,
                    x = 100.0,
                    y = 200.0,
                    appResource = null,
                    databaseResource = null,
                    domainResource = null,
                )
            val project =
                mockk<ProjectEntity>(relaxed = true) {
                    every { this@mockk.userId } returns userId
                }

            When("유효한 요청") {
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns project
                every { serviceRepository.save(any()) } returns mockk(relaxed = true)

                serviceService.createService(projectId, request)

                Then("서비스 생성") {
                    val entitySlot = slot<ServiceEntity>()
                    verify { serviceRepository.save(capture(entitySlot)) }
                    entitySlot.captured.projectId shouldBe projectId
                    entitySlot.captured.userId shouldBe userId
                    entitySlot.captured.name shouldBe request.name
                    entitySlot.captured.type shouldBe request.type
                    entitySlot.captured.x shouldBe request.x
                    entitySlot.captured.y shouldBe request.y
                }
            }

            When("프로젝트가 존재하지 않음") {
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.createService(projectId, request)
                        }
                    exception.error shouldBe ProjectError.PROJECT_NOT_FOUND
                }
            }

            When("프로젝트 접근 권한 없음") {
                val otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002")
                every { securityHolder.getUserId() } returns otherUserId
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns project

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.createService(projectId, request)
                        }
                    exception.error shouldBe ProjectError.PROJECT_ACCESS_DENIED
                }
            }
        }

        Given("getServices") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val projectId = UUID.fromString("10000000-0000-0000-0000-000000000001")
            val project =
                mockk<ProjectEntity>(relaxed = true) {
                    every { this@mockk.userId } returns userId
                }

            When("유효한 요청") {
                val serviceId1 = UUID.fromString("20000000-0000-0000-0000-000000000001")
                val serviceId2 = UUID.fromString("20000000-0000-0000-0000-000000000002")
                val services =
                    listOf(
                        mockk<ServiceEntity>(relaxed = true) {
                            every { id } returns serviceId1
                            every { name } returns "API Server"
                            every { type } returns ServiceType.APP
                            every { targetId } returns null
                        },
                        mockk<ServiceEntity>(relaxed = true) {
                            every { id } returns serviceId2
                            every { name } returns "Database"
                            every { type } returns ServiceType.DATABASE
                            every { targetId } returns null
                        },
                    )

                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns project
                every { serviceRepository.findAllByProjectIdAndDeletedAtIsNull(projectId) } returns services

                val result = serviceService.getServices(projectId)

                Then("서비스 목록 반환") {
                    result.size shouldBe 2
                    result[0].name shouldBe "API Server"
                    result[1].name shouldBe "Database"
                }
            }
        }

        Given("updateService") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val serviceId = UUID.fromString("20000000-0000-0000-0000-000000000001")
            val request =
                UpdateServiceRequest(
                    name = "Updated API Server",
                    description = "Updated description",
                    type = ServiceType.APP,
                    appResource = null,
                    databaseResource = null,
                    domainResource = null,
                )
            val service =
                mockk<ServiceEntity>(relaxed = true) {
                    every { this@mockk.userId } returns userId
                    every { name } returns "API Server"
                    every { projectId } returns UUID.fromString("10000000-0000-0000-0000-000000000001")
                    every { targetId } returns null
                }

            When("유효한 요청") {
                every { securityHolder.getUserId() } returns userId
                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns service

                serviceService.updateService(serviceId, request)

                Then("서비스 업데이트") {
                    verify {
                        service.update(
                            name = request.name,
                            description = request.description,
                            type = request.type,
                            targetId = null,
                        )
                    }
                }
            }

            When("서비스가 존재하지 않음") {
                every { securityHolder.getUserId() } returns userId
                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.updateService(serviceId, request)
                        }
                    exception.error shouldBe ServiceError.SERVICE_NOT_FOUND
                }
            }

            When("서비스 접근 권한 없음") {
                val otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002")
                every { securityHolder.getUserId() } returns otherUserId
                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns service

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.updateService(serviceId, request)
                        }
                    exception.error shouldBe ServiceError.SERVICE_ACCESS_DENIED
                }
            }
        }

        Given("updateServicePosition") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val serviceId = UUID.fromString("20000000-0000-0000-0000-000000000001")
            val request =
                UpdateServicePositionRequest(
                    x = 300.0,
                    y = 400.0,
                )
            val service =
                mockk<ServiceEntity>(relaxed = true) {
                    every { this@mockk.userId } returns userId
                }

            When("유효한 요청") {
                every { securityHolder.getUserId() } returns userId
                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns service

                serviceService.updateServicePosition(serviceId, request)

                Then("위치 업데이트") {
                    verify { service.updatePosition(request.x, request.y) }
                }
            }
        }

        Given("deleteService") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val serviceId = UUID.fromString("20000000-0000-0000-0000-000000000001")
            val service =
                mockk<ServiceEntity>(relaxed = true) {
                    every { this@mockk.userId } returns userId
                    every { isDeleted } returns false
                    every { targetId } returns null
                    every { delete() } just Runs
                }

            When("유효한 요청") {
                every { securityHolder.getUserId() } returns userId
                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns service
                every { connectionRepository.findAllBySourceIdOrTargetIdAndDeletedAtIsNull(serviceId, serviceId) } returns emptyList()

                serviceService.deleteService(serviceId)

                Then("서비스 삭제") {
                    verify { service.delete() }
                }
            }

            When("이미 삭제된 서비스") {
                every { securityHolder.getUserId() } returns userId
                every { serviceRepository.findByIdAndDeletedAtIsNull(serviceId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.deleteService(serviceId)
                        }
                    exception.error shouldBe ServiceError.SERVICE_NOT_FOUND
                }
            }
        }
    })
