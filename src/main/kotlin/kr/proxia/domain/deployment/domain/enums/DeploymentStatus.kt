package kr.proxia.domain.deployment.domain.enums

enum class DeploymentStatus {
    PENDING,
    QUEUED,
    CLONING,
    BUILDING,
    DEPLOYING,
    RUNNING,
    FAILED,
    CANCELLED,
}
