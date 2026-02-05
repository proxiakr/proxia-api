package kr.proxia.client.aws.s3

import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.S3Client as AwsS3Client

@Component
class S3Client internal constructor(
    private val awsS3Client: AwsS3Client,
) {
    fun ping(): Boolean =
        runCatching {
            awsS3Client.listBuckets()
            true
        }.getOrDefault(false)

    fun upload(
        bucketName: String,
        key: String,
        content: String,
    ): Boolean =
        runCatching {
            awsS3Client.putObject(
                PutObjectRequest
                    .builder()
                    .bucket(bucketName)
                    .key(key)
                    .build(),
                RequestBody.fromString(content),
            )
            true
        }.getOrDefault(false)
}
