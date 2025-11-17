package kr.proxia.domain.service.application.event

import java.util.UUID

data class ServiceCreatedEvent(
    val serviceId: UUID,
)
