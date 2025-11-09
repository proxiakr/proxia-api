package kr.proxia.domain.service.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kr.proxia.domain.project.domain.entity.ProjectEntity
import kr.proxia.domain.project.domain.error.ProjectError
import kr.proxia.domain.project.domain.repository.ProjectRepository
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

class ServiceServiceTest :
    BehaviorSpec({
        val serviceRepository = mockk<ServiceRepository>()
        val projectRepository = mockk<ProjectRepository>()
        val securityHolder = mockk<SecurityHolder>()

        val serviceService =
            ServiceService(
                serviceRepository,
                projectRepository,
                securityHolder,
            )

        Given("createService") {
            val userId = 1L
            val projectId = 10L
            val request =
                CreateServiceRequest(
                    name = "API Server",
                    description = "Main API Server",
                    type = ServiceType.API,
                    x = 100.0,
                    y = 200.0,
                )
            val project =
                mockk<ProjectEntity>(relaxed = true) {
                    every { this@mockk.userId } returns userId
                }

            When("유효한 요청") {
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdOrNull(projectId) } returns project
                every { serviceRepository.existsByProjectIdAndName(projectId, request.name) } returns false
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
                every { projectRepository.findByIdOrNull(projectId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.createService(projectId, request)
                        }
                    exception.error shouldBe ProjectError.PROJECT_NOT_FOUND
                }
            }

            When("프로젝트 접근 권한 없음") {
                val otherUserId = 2L
                every { securityHolder.getUserId() } returns otherUserId
                every { projectRepository.findByIdOrNull(projectId) } returns project

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.createService(projectId, request)
                        }
                    exception.error shouldBe ProjectError.PROJECT_ACCESS_DENIED
                }
            }

            When("중복된 서비스 이름") {
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdOrNull(projectId) } returns project
                every { serviceRepository.existsByProjectIdAndName(projectId, request.name) } returns true

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.createService(projectId, request)
                        }
                    exception.error shouldBe ServiceError.SERVICE_NAME_ALREADY_EXISTS
                }
            }
        }

        Given("getServices") {
            val userId = 1L
            val projectId = 10L
            val project =
                mockk<ProjectEntity>(relaxed = true) {
                    every { this@mockk.userId } returns userId
                }

            When("유효한 요청") {
                val services =
                    listOf(
                        mockk<ServiceEntity>(relaxed = true) {
                            every { id } returns 1L
                            every { name } returns "API Server"
                            every { type } returns ServiceType.API
                        },
                        mockk<ServiceEntity>(relaxed = true) {
                            every { id } returns 2L
                            every { name } returns "Database"
                            every { type } returns ServiceType.DATABASE
                        },
                    )

                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdOrNull(projectId) } returns project
                every { serviceRepository.findAllByProjectId(projectId) } returns services

                val result = serviceService.getServices(projectId)

                Then("서비스 목록 반환") {
                    result.size shouldBe 2
                    result[0].name shouldBe "API Server"
                    result[1].name shouldBe "Database"
                }
            }
        }

        Given("updateService") {
            val userId = 1L
            val serviceId = 5L
            val request =
                UpdateServiceRequest(
                    name = "Updated API Server",
                    description = "Updated description",
                    type = ServiceType.API,
                )
            val service =
                mockk<ServiceEntity>(relaxed = true) {
                    every { this@mockk.userId } returns userId
                    every { name } returns "API Server"
                    every { projectId } returns 10L
                }

            When("유효한 요청") {
                every { securityHolder.getUserId() } returns userId
                every { serviceRepository.findByIdOrNull(serviceId) } returns service
                every { serviceRepository.existsByProjectIdAndName(any(), any()) } returns false

                serviceService.updateService(serviceId, request)

                Then("서비스 업데이트") {
                    verify { service.update(request.name, request.description, request.type) }
                }
            }

            When("서비스가 존재하지 않음") {
                every { securityHolder.getUserId() } returns userId
                every { serviceRepository.findByIdOrNull(serviceId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.updateService(serviceId, request)
                        }
                    exception.error shouldBe ServiceError.SERVICE_NOT_FOUND
                }
            }

            When("서비스 접근 권한 없음") {
                val otherUserId = 2L
                every { securityHolder.getUserId() } returns otherUserId
                every { serviceRepository.findByIdOrNull(serviceId) } returns service

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
            val userId = 1L
            val serviceId = 5L
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
                every { serviceRepository.findByIdOrNull(serviceId) } returns service

                serviceService.updateServicePosition(serviceId, request)

                Then("위치 업데이트") {
                    verify { service.updatePosition(request.x, request.y) }
                }
            }
        }

        Given("deleteService") {
            val userId = 1L
            val serviceId = 5L
            val service =
                mockk<ServiceEntity>(relaxed = true) {
                    every { this@mockk.userId } returns userId
                    every { isDeleted } returns false
                }

            When("유효한 요청") {
                every { securityHolder.getUserId() } returns userId
                every { serviceRepository.findByIdOrNull(serviceId) } returns service

                serviceService.deleteService(serviceId)

                Then("서비스 삭제") {
                    verify { service.delete() }
                }
            }

            When("이미 삭제된 서비스") {
                every { securityHolder.getUserId() } returns userId
                every { serviceRepository.findByIdOrNull(serviceId) } returns service
                every { service.isDeleted } returns true

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            serviceService.deleteService(serviceId)
                        }
                    exception.error shouldBe ServiceError.SERVICE_ALREADY_DELETED
                }
            }
        }
    })
