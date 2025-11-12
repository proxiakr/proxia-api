package kr.proxia.domain.connection.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
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
import java.util.UUID

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
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val projectId = UUID.fromString("10000000-0000-0000-0000-000000000001")
            val sourceId = UUID.fromString("20000000-0000-0000-0000-000000000001")
            val targetId = UUID.fromString("20000000-0000-0000-0000-000000000002")
            val request =
                CreateConnectionRequest(
                    sourceId = sourceId,
                    targetId = targetId,
                    type = ConnectionType.ROUTING,
                    label = "REST API",
                    weight = 1,
                )

            When("유효한 요청") {
                every { securityHolder.getUserId() } returns userId
                every {
                    projectRepository.findByIdAndDeletedAtIsNull(projectId)
                } returns
                    mockk<ProjectEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                    }
                every {
                    serviceRepository.findByIdAndDeletedAtIsNull(sourceId)
                } returns
                    mockk<ServiceEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                    }
                every {
                    serviceRepository.findByIdAndDeletedAtIsNull(targetId)
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
                    projectRepository.findByIdAndDeletedAtIsNull(projectId)
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
                    projectRepository.findByIdAndDeletedAtIsNull(projectId)
                } returns
                    mockk<ProjectEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                    }
                every {
                    serviceRepository.findByIdAndDeletedAtIsNull(sourceId)
                } returns
                    mockk<ServiceEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                    }
                every {
                    serviceRepository.findByIdAndDeletedAtIsNull(targetId)
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
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val connectionId = UUID.fromString("30000000-0000-0000-0000-000000000001")
            val projectId = UUID.fromString("10000000-0000-0000-0000-000000000001")
            val request =
                UpdateConnectionRequest(
                    type = ConnectionType.NETWORK,
                    label = "Network Connection",
                    weight = 2,
                )

            When("유효한 요청") {
                val connection =
                    mockk<ConnectionEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                    }
                every { securityHolder.getUserId() } returns userId
                every { connectionRepository.findByIdAndDeletedAtIsNull(connectionId) } returns connection
                every {
                    projectRepository.findByIdAndDeletedAtIsNull(projectId)
                } returns
                    mockk<ProjectEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                    }

                connectionService.updateConnection(connectionId, request)

                Then("연결 업데이트") {
                    verify { connection.update(request.type, request.label, request.weight) }
                }
            }
        }

        Given("deleteConnection") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val connectionId = UUID.fromString("30000000-0000-0000-0000-000000000001")
            val projectId = UUID.fromString("10000000-0000-0000-0000-000000000001")

            When("유효한 요청") {
                val connection =
                    mockk<ConnectionEntity>(relaxed = true) {
                        every { this@mockk.projectId } returns projectId
                        every { delete() } just Runs
                    }
                every { securityHolder.getUserId() } returns userId
                every { connectionRepository.findByIdAndDeletedAtIsNull(connectionId) } returns connection
                every {
                    projectRepository.findByIdAndDeletedAtIsNull(projectId)
                } returns
                    mockk<ProjectEntity>(relaxed = true) {
                        every { this@mockk.userId } returns userId
                    }

                connectionService.deleteConnection(connectionId)

                Then("연결 삭제") {
                    verify { connection.delete() }
                }
            }
        }
    })
