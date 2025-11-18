package kr.proxia.domain.node.application.scheduler

import kr.proxia.domain.node.domain.entity.NodeEntity
import kr.proxia.domain.node.domain.enums.NodeStatus
import kr.proxia.domain.node.domain.error.NodeError
import kr.proxia.domain.node.domain.repository.NodeRepository
import kr.proxia.global.error.BusinessException
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

@Service
class NodeScheduler(
    private val nodeRepository: NodeRepository,
) {
    private val counter = AtomicInteger(0)

    fun chooseNode(): NodeEntity {
        val activeNodes = nodeRepository.findByStatus(NodeStatus.ACTIVE)

        if (activeNodes.isEmpty()) {
            throw BusinessException(NodeError.NO_ACTIVE_NODE_AVAILABLE)
        }

        val minCount = activeNodes.minOf { it.containerCount }

        val leastLoadedNodes = activeNodes.filter { it.containerCount == minCount }

        val index = counter.getAndIncrement().mod(leastLoadedNodes.size)

        return leastLoadedNodes[index]
    }
}
