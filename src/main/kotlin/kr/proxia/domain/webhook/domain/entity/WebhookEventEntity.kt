package kr.proxia.domain.webhook.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(
    name = "webhook_events",
    indexes = [
        Index(name = "idx_webhook_events_service_deleted", columnList = "serviceId, deletedAt"),
        Index(name = "idx_webhook_events_delivery_id", columnList = "deliveryId"),
    ],
)
class WebhookEventEntity(
    val serviceId: Long?,
    val event: String,
    @Column(columnDefinition = "TEXT")
    val payload: String,
    val deliveryId: String?,
    success: Boolean = true,
    errorMessage: String? = null,
) : BaseEntity() {
    var success: Boolean = success
        protected set

    @Column(columnDefinition = "TEXT")
    var errorMessage: String? = errorMessage
        protected set

    fun markAsFailed(errorMessage: String) {
        this.success = false
        this.errorMessage = errorMessage
    }
}
