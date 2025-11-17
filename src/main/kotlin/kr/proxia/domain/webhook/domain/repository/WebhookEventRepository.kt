package kr.proxia.domain.webhook.domain.repository

import kr.proxia.domain.webhook.domain.entity.WebhookEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WebhookEventRepository : JpaRepository<WebhookEventEntity, UUID>
