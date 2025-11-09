package kr.proxia.domain.project.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(name = "projects")
class ProjectEntity(
    val userId: Long,
    name: String,
    slug: String,
) : BaseEntity() {
    var slug: String = slug
        protected set

    var name: String = name
        protected set

    fun update(
        name: String = this.name,
        slug: String = this.slug,
    ) {
        this.name = name
        this.slug = slug
    }
}
