package kr.proxia.domain.project.application.service

import io.mockk.every
import io.mockk.mockk
import kr.proxia.domain.project.domain.entity.ProjectEntity
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.security.holder.SecurityHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull

class ProjectServiceTest {
    private lateinit var projectService: ProjectService
    private lateinit var userRepository: UserRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var securityHolder: SecurityHolder
    private val userId = 1L

    @BeforeEach
    fun setUp() {
        userRepository = mockk<UserRepository>()
        projectRepository = mockk<ProjectRepository>()
        securityHolder = mockk<SecurityHolder>()
        projectService = ProjectService(userRepository, projectRepository, securityHolder)
    }

    @Test
    fun `프로젝트 생성 시 ID가 생성되어야 한다`() {
        every { projectRepository.save(any()) } returnsArgument 0

        val project = projectRepository.save(
            ProjectEntity(
                userId = userId,
                name = "name",
                slug = "slug",
            )
        )

        assertThat(project.id).isNotNull
    }

    @Test
    fun `프로젝트 조회 시 생성자가 아니면 조회할 수 없다`() {
        every { projectRepository.save(any()) } returnsArgument 0
        every { projectRepository.findByIdOrNull(any()) } returnsArgument 0
        every { userRepository.findByIdOrNull(any()) } returnsArgument 0
        every { securityHolder.getUserId() } returns userId

        val project = projectRepository.save(
            ProjectEntity(
                userId = 2L,
                name = "name",
                slug = "slug",
            )
        )

        assertThrows<IllegalArgumentException> { projectService.getProject(project.id) }
    }

    @Test
    fun `프로젝트 생성자가 아니라면 프로젝트를 삭제할 수 없다`() {
        every { securityHolder.getUserId() } returns userId
        every { projectRepository.findByIdOrNull(any()) } returnsArgument 0
        every { projectRepository.save(any()) } returnsArgument 0

        val project = projectRepository.save(
            ProjectEntity(
                userId = 2L,
                name = "name",
                slug = "slug",
            )
        )

        assertThrows<IllegalArgumentException> { projectService.deleteProject(project.id) }
    }

    @Test
    fun `프로젝트 삭제 시 deletedAt 필드가 null이 되지 않아야 한다`() {
        every { projectRepository.save(any()) } returnsArgument 0
        every { projectRepository.deleteById(any()) } returnsArgument 0

        val project = projectRepository.save(
            ProjectEntity(
                userId = userId,
                name = "name",
                slug = "slug",
            )
        )

        projectRepository.deleteById(project.id)

        assertThat(project.deletedAt).isNotNull
    }
}