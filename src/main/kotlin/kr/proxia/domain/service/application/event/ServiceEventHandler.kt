package kr.proxia.domain.service.application.event

import kr.proxia.domain.deployment.application.service.DeploymentService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ServiceEventHandler(
    private val deploymentService: DeploymentService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ServiceCreatedEvent) {
    }
}
