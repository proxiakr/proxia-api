package kr.proxia.global.container

data class ContainerSpec(
    val name: String,
    val image: String,
    val ports: List<PortMapping> = emptyList(),
    val env: Map<String, String> = emptyMap(),
    val volumes: List<VolumeMapping> = emptyList(),
    val command: List<String>? = null,
) {
    data class PortMapping(
        val internal: Int,
        val host: Int? = null,
    )

    data class VolumeMapping(
        val hostPath: String,
        val containerPath: String,
    )
}
