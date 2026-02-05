package kr.proxia.client.aws.s3

data class S3Properties(
    val region: String = "ap-northeast-2",
    val bucketName: String = "",
    val logPrefix: String = "logs",
    val maxAttempts: Int = 3,
    val connectionTimeout: Long = 5000,
    val requestTimeout: Long = 60000,
)
