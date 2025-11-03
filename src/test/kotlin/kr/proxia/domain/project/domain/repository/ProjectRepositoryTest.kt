package kr.proxia.domain.project.domain.repository

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class ProjectRepositoryTest {
    private lateinit var projectRepository: ProjectRepository
}