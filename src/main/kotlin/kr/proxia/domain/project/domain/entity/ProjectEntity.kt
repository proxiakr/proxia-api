package kr.proxia.domain.project.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.proxia.global.jpa.common.BaseEntity
import java.util.UUID

@Entity
@Table(
    name = "projects",
    indexes = [
        Index(name = "idx_projects_user_deleted", columnList = "userId, deletedAt"),
        Index(name = "idx_projects_slug_deleted", columnList = "slug, deletedAt"),
    ],
)
class ProjectEntity(
    val userId: UUID,
    name: String,
    slug: String,
) : BaseEntity() {
    var name: String = name
        protected set

    var slug: String = slug
        protected set

    fun update(
        name: String = this.name,
        slug: String = this.slug,
    ) {
        this.name = name
        this.slug = slug
    }
}
