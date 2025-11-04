package kr.proxia.domain.project.domain.repository

import kr.proxia.domain.project.domain.entity.ProjectEntity
import kr.proxia.global.jpa.config.JpaConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest

@DataJpaTest
@Import(JpaConfig::class)
class ProjectRepositoryTest {
    @Autowired
    private lateinit var projectRepository: ProjectRepository
    private val userId = 1L

    @Test
    fun `프로젝트 저장`() {
        val project = projectRepository.save(
            ProjectEntity(
                userId = userId,
                name = "name",
                slug = "slug",
            )
        )

        assertThat(project.id).isNotNull()
    }

    @Test
    fun `유저 ID로 목록 조회`() {
        for (i in 1..10) {
            projectRepository.save(
                ProjectEntity(
                    userId = userId,
                    name = "name$i",
                    slug = "slug$i",
                )
            )
        }

        val projects = projectRepository.findAllByUserId(userId, PageRequest.of(0, 10)).toList()

        assertThat(projects).hasSize(10)

        for (i in 1..10) {
            assertThat(projects[i - 1].name).isEqualTo("name$i")
            assertThat(projects[i - 1].slug).isEqualTo("slug$i")
        }
    }

    @Test
    fun `프로젝트 수정`() {
        val before = projectRepository.save(
            ProjectEntity(
                userId = userId,
                name = "name",
                slug = "slug",
            )
        )

        before.update(
            name = "updated name",
            slug = "updated slug",
        )

        val after = projectRepository.findById(before.id).get()

        assertThat(after.name).isEqualTo("updated name")
        assertThat(after.slug).isEqualTo("updated slug")
    }

    @Test
    fun `프로젝트 삭제`() {
        val project = projectRepository.save(
            ProjectEntity(
                userId = userId,
                name = "name",
                slug = "slug",
            )
        )

        val projectId = project.id

        projectRepository.deleteById(projectId)

        assertThat(projectRepository.existsById(projectId)).isFalse()
    }

    @Test
    fun `slug로 프로젝트 존재 여부 확인`() {
        val project = ProjectEntity(
            userId = userId,
            name = "name",
            slug = "slug",
        )

        projectRepository.save(project)

        val exists = projectRepository.existsBySlug(project.slug)

        assertThat(exists).isTrue
    }
}