package kr.proxia.domain.connection.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kr.proxia.domain.connection.domain.entity.ConnectionEntity
import kr.proxia.domain.connection.domain.enums.ConnectionType
import kr.proxia.domain.connection.domain.error.ConnectionError
import kr.proxia.domain.connection.domain.repository.ConnectionRepository
import kr.proxia.domain.connection.presentation.request.CreateConnectionRequest
import kr.proxia.domain.connection.presentation.request.UpdateConnectionRequest
import kr.proxia.domain.project.domain.entity.ProjectEntity
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.service.domain.entity.ServiceEntity
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.repository.findByIdOrNull

class ConnectionServiceTest :
    BehaviorSpec({
        val connectionRepository = mockk<ConnectionRepository>()
        val serviceRepository = mockk<ServiceRepository>()
        val projectRepository = mockk<ProjectRepository>()
        val securityHolder = mockk<SecurityHolder>()

        val connectionService =
            ConnectionService(
                connectionRepository,
                serviceRepository,
                projectRepository,
                securityHolder,
            )

        Given("createConnection") {
            val userId = 1L
            val projectId = 10L
            val sourceId = 20L
            val targetId = 21L
            val request =
                CreateConnectionRequest(
                    sourceId = sourceId,
                    targetId = targetId,
                    type = ConnectionType.HTTP,
                    label = "REST API",
                )

            When("유효한 요청") {
                every { securityHolder.getUserId() } returns userId
                every {
                    projectRepository.findByIdOrNull(projectId)
                } returns
                    mockk<ProjectEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                    }
                every {
                    serviceRepository.findByIdOrNull(sourceId)
                } returns
                    mockk<ServiceEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                    }
                every {
                    serviceRepository.findByIdOrNull(targetId)
                } returns
                    mockk<ServiceEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                    }
                every { connectionRepository.existsBySourceIdAndTargetIdAndDeletedAtIsNull(sourceId, targetId) } returns false
                every { connectionRepository.save(any()) } returns mockk(relaxed = true)

                connectionService.createConnection(projectId, request)

                Then("연결 생성") {
                    val slot = slot<ConnectionEntity>()
                    verify { connectionRepository.save(capture(slot)) }
                    slot.captured.projectId shouldBe projectId
                    slot.captured.sourceId shouldBe sourceId
                    slot.captured.targetId shouldBe targetId
                }
            }

            When("자기 자신에게 연결") {
                every { securityHolder.getUserId() } returns userId
                every {
                    projectRepository.findByIdOrNull(projectId)
                } returns
                    mockk<ProjectEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                    }

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            connectionService.createConnection(
                                projectId,
                                request.copy(targetId = sourceId),
                            )
                        }
                    exception.error shouldBe ConnectionError.INVALID_CONNECTION
                }
            }

            When("이미 존재하는 연결") {
                every { securityHolder.getUserId() } returns userId
                every {
                    projectRepository.findByIdOrNull(projectId)
                } returns
                    mockk<ProjectEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                    }
                every {
                    serviceRepository.findByIdOrNull(sourceId)
                } returns
                    mockk<ServiceEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                    }
                every {
                    serviceRepository.findByIdOrNull(targetId)
                } returns
                    mockk<ServiceEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                    }
                every { connectionRepository.existsBySourceIdAndTargetIdAndDeletedAtIsNull(sourceId, targetId) } returns true

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            connectionService.createConnection(projectId, request)
                        }
                    exception.error shouldBe ConnectionError.CONNECTION_ALREADY_EXISTS
                }
            }
        }

        Given("updateConnection") {
            val userId = 1L
            val connectionId = 5L
            val projectId = 10L
            val request =
                UpdateConnectionRequest(
                    type = ConnectionType.WEBSOCKET,
                    label = "WebSocket",
                )

            When("유효한 요청") {
                val connection =
                    mockk<ConnectionEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                    }
                every { securityHolder.getUserId() } returns userId
                every { connectionRepository.findByIdOrNull(connectionId) } returns connection
                every {
                    projectRepository.findByIdOrNull(projectId)
                } returns
                    mockk<ProjectEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                    }

                connectionService.updateConnection(connectionId, request)

                Then("연결 업데이트") {
                    verify { connection.update(request.type, request.label) }
                }
            }
        }

        Given("deleteConnection") {
            val userId = 1L
            val connectionId = 5L
            val projectId = 10L

            When("유효한 요청") {
                val connection =
                    mockk<ConnectionEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                    }
                every { securityHolder.getUserId() } returns userId
                every { connectionRepository.findByIdOrNull(connectionId) } returns connection
                every {
                    projectRepository.findByIdOrNull(projectId)
                } returns
                    mockk<ProjectEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                    }
                every { connectionRepository.delete(connection) } returns Unit

                connectionService.deleteConnection(connectionId)

                Then("연결 삭제") {
                    verify { connectionRepository.delete(connection) }
                }
            }
        }
    })
