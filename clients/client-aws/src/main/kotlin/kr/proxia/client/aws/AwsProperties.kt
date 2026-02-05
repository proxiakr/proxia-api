package kr.proxia.client.aws

import kr.proxia.client.aws.s3.S3Properties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aws")
data class AwsProperties(
    val s3: S3Properties,
)
