package kr.proxia.domain.project.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(name = "projects")
class ProjectEntity(
    val userId: Long,
    name: String,
) : BaseEntity() {
    var name: String = name
        protected set

    fun update(name: String = this.name) {
        this.name = name
    }
}
