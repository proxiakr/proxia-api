package kr.proxia.domain.project.application.service

import kr.proxia.domain.project.domain.repository.ProjectRepository
import org.springframework.stereotype.Service

@Service
class ProjectService(private val projectRepository: ProjectRepository) {
}