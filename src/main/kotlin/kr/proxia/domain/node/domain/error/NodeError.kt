package kr.proxia.domain.node.domain.error

import kr.proxia.global.error.BaseError
import org.springframework.http.HttpStatus

enum class NodeError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    NODE_NOT_FOUND(HttpStatus.NOT_FOUND, "Node not found"),
    NO_ACTIVE_NODE_AVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "No active nodes available for deployment"),
}
