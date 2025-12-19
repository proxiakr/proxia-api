package kr.proxia.domain.project.application.service

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
import kr.proxia.domain.project.presentation.request.CreateProjectRequest
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.domain.user.domain.entity.UserEntity
import kr.proxia.domain.user.domain.error.UserError
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.support.OffsetLimit
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.UUID

class ProjectServiceTest :
    BehaviorSpec({
        val userRepository = mockk<UserRepository>()
        val projectRepository = mockk<ProjectRepository>()
        val serviceRepository = mockk<ServiceRepository>()
        val connectionRepository = mockk<ConnectionRepository>()
        val securityHolder = mockk<SecurityHolder>()

        val projectService =
            ProjectService(
                userRepository,
                projectRepository,
                serviceRepository,
                connectionRepository,
                securityHolder,
            )

        Given("createProject") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val request =
                CreateProjectRequest(
                    name = "Test Project",
                    slug = "test-project",
                )

            When("유효한 요청") {
                val projectSlot = slot<ProjectEntity>()
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.existsBySlugAndDeletedAtIsNull(request.slug) } returns false
                every { projectRepository.save(capture(projectSlot)) } returns mockk(relaxed = true)

                projectService.createProject(request)

                Then("프로젝트 생성") {
                    projectSlot.captured.userId shouldBe userId
                    projectSlot.captured.name shouldBe request.name
                    projectSlot.captured.slug shouldBe request.slug
                }
            }

            When("이미 존재하는 slug") {
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.existsBySlugAndDeletedAtIsNull(request.slug) } returns true

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.createProject(request)
                        }
                    exception.error shouldBe ProjectError.SlugAlreadyExists
                }
            }
        }

        Given("getProjects") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val offsetLimit = OffsetLimit(offset = 0, limit = 10)

            When("프로젝트 목록 조회") {
                val now = LocalDateTime.now()
                val projectId1 = UUID.fromString("10000000-0000-0000-0000-000000000001")
                val projectId2 = UUID.fromString("10000000-0000-0000-0000-000000000002")
                val project1 = mockk<ProjectEntity>(relaxed = true)
                every { project1.id } returns projectId1
                every { project1.name } returns "Project 1"
                every { project1.slug } returns "project-1"
                every { project1.createdAt } returns now
                every { project1.updatedAt } returns now

                val project2 = mockk<ProjectEntity>(relaxed = true)
                every { project2.id } returns projectId2
                every { project2.name } returns "Project 2"
                every { project2.slug } returns "project-2"
                every { project2.createdAt } returns now
                every { project2.updatedAt } returns now

                val projects = listOf(project1, project2)
                val page = PageImpl(projects, PageRequest.of(0, 10), 2)

                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findAllByUserIdAndDeletedAtIsNull(userId, offsetLimit.toPageable()) } returns page

                val result = projectService.getProjects(offsetLimit)

                Then("프로젝트 목록 반환") {
                    result.content.size shouldBe 2
                    result.content[0].id shouldBe projectId1
                    result.content[1].id shouldBe projectId2
                    result.hasNext shouldBe false
                }
            }
        }

        Given("getProject") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val projectId = UUID.fromString("10000000-0000-0000-0000-000000000001")
            val now = LocalDateTime.now()

            When("프로젝트 조회 성공") {
                val user = mockk<UserEntity>(relaxed = true)
                every { user.id } returns userId
                every { user.name } returns "Test User"
                every { user.avatarUrl } returns "https://example.com/avatar.jpg"

                val project = mockk<ProjectEntity>(relaxed = true)
                every { project.id } returns projectId
                every { project.userId } returns userId
                every { project.name } returns "Test Project"
                every { project.slug } returns "test-project"
                every { project.createdAt } returns now
                every { project.updatedAt } returns now

                every { securityHolder.getUserId() } returns userId
                every { userRepository.findByIdOrNull(userId) } returns user
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns project

                val result = projectService.getProject(projectId)

                Then("프로젝트 상세 정보 반환") {
                    result.id shouldBe projectId
                    result.name shouldBe "Test Project"
                    result.slug shouldBe "test-project"
                    result.user.id shouldBe userId
                }
            }

            When("사용자를 찾을 수 없음") {
                every { securityHolder.getUserId() } returns userId
                every { userRepository.findByIdOrNull(userId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.getProject(projectId)
                        }
                    exception.error shouldBe UserError.NotFound
                }
            }

            When("프로젝트를 찾을 수 없음") {
                val user = mockk<UserEntity>(relaxed = true)
                every { securityHolder.getUserId() } returns userId
                every { userRepository.findByIdOrNull(userId) } returns user
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.getProject(projectId)
                        }
                    exception.error shouldBe ProjectError.NotFound
                }
            }

            When("프로젝트 소유자가 아님") {
                val user = mockk<UserEntity>(relaxed = true)
                val otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002")
                val project = mockk<ProjectEntity>(relaxed = true)
                every { project.userId } returns otherUserId

                every { securityHolder.getUserId() } returns userId
                every { userRepository.findByIdOrNull(userId) } returns user
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns project

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.getProject(projectId)
                        }
                    exception.error shouldBe ProjectError.AccessDenied
                }
            }
        }

        Given("deleteProject") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
            val projectId = UUID.fromString("10000000-0000-0000-0000-000000000001")

            When("프로젝트 삭제 성공") {
                val project = mockk<ProjectEntity>(relaxed = true)
                every { project.userId } returns userId
                every { project.isDeleted } returns false
                every { project.delete() } just Runs

                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns project
                every { serviceRepository.findAllByProjectIdAndDeletedAtIsNull(projectId) } returns emptyList()
                every { connectionRepository.findAllByProjectIdAndDeletedAtIsNull(projectId) } returns emptyList()

                projectService.deleteProject(projectId)

                Then("프로젝트 삭제") {
                    verify { project.delete() }
                }
            }

            When("프로젝트를 찾을 수 없음") {
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.deleteProject(projectId)
                        }
                    exception.error shouldBe ProjectError.NotFound
                }
            }

            When("프로젝트 소유자가 아님") {
                val otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002")
                val project = mockk<ProjectEntity>(relaxed = true)
                every { project.userId } returns otherUserId

                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns project

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.deleteProject(projectId)
                        }
                    exception.error shouldBe ProjectError.AccessDenied
                }
            }

            When("이미 삭제된 프로젝트") {
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdAndDeletedAtIsNull(projectId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.deleteProject(projectId)
                        }
                    exception.error shouldBe ProjectError.NotFound
                }
            }
        }
    })
