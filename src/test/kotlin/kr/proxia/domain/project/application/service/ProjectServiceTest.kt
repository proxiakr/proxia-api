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
import kr.proxia.domain.project.domain.entity.ProjectEntity
import kr.proxia.domain.project.domain.error.ProjectError
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.project.presentation.request.CreateProjectRequest
import kr.proxia.domain.user.domain.entity.UserEntity
import kr.proxia.domain.user.domain.error.UserError
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.response.OffsetLimit
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

class ProjectServiceTest :
    BehaviorSpec({
        val userRepository = mockk<UserRepository>()
        val projectRepository = mockk<ProjectRepository>()
        val securityHolder = mockk<SecurityHolder>()

        val projectService = ProjectService(userRepository, projectRepository, securityHolder)

        Given("createProject") {
            val userId = 1L
            val request =
                CreateProjectRequest(
                    name = "Test Project",
                )

            When("유효한 요청") {
                val projectSlot = slot<ProjectEntity>()
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.save(capture(projectSlot)) } returns mockk(relaxed = true)

                projectService.createProject(request)

                Then("프로젝트 생성") {
                    projectSlot.captured.userId shouldBe userId
                    projectSlot.captured.name shouldBe request.name
                }
            }
        }

        Given("getProjects") {
            val userId = 1L
            val offsetLimit = OffsetLimit(offset = 0, limit = 10)

            When("프로젝트 목록 조회") {
                val now = LocalDateTime.now()
                val project1 = mockk<ProjectEntity>(relaxed = true)
                every { project1.id } returns 1L
                every { project1.name } returns "Project 1"
                every { project1.createdAt } returns now
                every { project1.updatedAt } returns now

                val project2 = mockk<ProjectEntity>(relaxed = true)
                every { project2.id } returns 2L
                every { project2.name } returns "Project 2"
                every { project2.createdAt } returns now
                every { project2.updatedAt } returns now

                val projects = listOf(project1, project2)
                val page = PageImpl(projects, PageRequest.of(0, 10), 2)

                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findAllByUserId(userId, offsetLimit.toPageable()) } returns page

                val result = projectService.getProjects(offsetLimit)

                Then("프로젝트 목록 반환") {
                    result.content.size shouldBe 2
                    result.content[0].id shouldBe 1L
                    result.content[1].id shouldBe 2L
                    result.hasNext shouldBe false
                }
            }
        }

        Given("getProject") {
            val userId = 1L
            val projectId = 1L
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
                every { project.createdAt } returns now
                every { project.updatedAt } returns now

                every { securityHolder.getUserId() } returns userId
                every { userRepository.findByIdOrNull(userId) } returns user
                every { projectRepository.findByIdOrNull(projectId) } returns project

                val result = projectService.getProject(projectId)

                Then("프로젝트 상세 정보 반환") {
                    result.id shouldBe projectId
                    result.name shouldBe "Test Project"
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
                    exception.error shouldBe UserError.USER_NOT_FOUND
                }
            }

            When("프로젝트를 찾을 수 없음") {
                val user = mockk<UserEntity>(relaxed = true)
                every { securityHolder.getUserId() } returns userId
                every { userRepository.findByIdOrNull(userId) } returns user
                every { projectRepository.findByIdOrNull(projectId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.getProject(projectId)
                        }
                    exception.error shouldBe ProjectError.PROJECT_NOT_FOUND
                }
            }

            When("프로젝트 소유자가 아님") {
                val user = mockk<UserEntity>(relaxed = true)
                val project = mockk<ProjectEntity>(relaxed = true)
                every { project.userId } returns 2L

                every { securityHolder.getUserId() } returns userId
                every { userRepository.findByIdOrNull(userId) } returns user
                every { projectRepository.findByIdOrNull(projectId) } returns project

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.getProject(projectId)
                        }
                    exception.error shouldBe ProjectError.PROJECT_ACCESS_DENIED
                }
            }
        }

        Given("deleteProject") {
            val userId = 1L
            val projectId = 1L

            When("프로젝트 삭제 성공") {
                val project = mockk<ProjectEntity>(relaxed = true)
                every { project.userId } returns userId
                every { project.isDeleted } returns false
                every { project.delete() } just Runs

                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdOrNull(projectId) } returns project

                projectService.deleteProject(projectId)

                Then("프로젝트 삭제") {
                    verify { project.delete() }
                }
            }

            When("프로젝트를 찾을 수 없음") {
                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdOrNull(projectId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.deleteProject(projectId)
                        }
                    exception.error shouldBe ProjectError.PROJECT_NOT_FOUND
                }
            }

            When("프로젝트 소유자가 아님") {
                val project = mockk<ProjectEntity>(relaxed = true)
                every { project.userId } returns 2L

                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdOrNull(projectId) } returns project

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.deleteProject(projectId)
                        }
                    exception.error shouldBe ProjectError.PROJECT_ACCESS_DENIED
                }
            }

            When("이미 삭제된 프로젝트") {
                val project = mockk<ProjectEntity>(relaxed = true)
                every { project.userId } returns userId
                every { project.isDeleted } returns true

                every { securityHolder.getUserId() } returns userId
                every { projectRepository.findByIdOrNull(projectId) } returns project

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            projectService.deleteProject(projectId)
                        }
                    exception.error shouldBe ProjectError.PROJECT_ALREADY_DELETED
                }
            }
        }
    })
