package kr.proxia.client.docker

data class VolumeBinding(
    val volumeName: String,
    val containerPath: String,
)
