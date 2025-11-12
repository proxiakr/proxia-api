package kr.proxia.domain.webhook.domain.repository

import kr.proxia.domain.webhook.domain.entity.WebhookEventEntity
import org.springframework.data.jpa.repository.JpaRepository

interface WebhookEventRepository : JpaRepository<WebhookEventEntity, Long> {
    fun findAllByServiceIdOrderByCreatedAtDesc(serviceId: Long): List<WebhookEventEntity>
}
