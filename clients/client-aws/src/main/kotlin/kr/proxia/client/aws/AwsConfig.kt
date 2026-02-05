package kr.proxia.client.aws

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
@EnableConfigurationProperties(AwsProperties::class)
internal class AwsConfig(
    private val awsProperties: AwsProperties,
) {
    @Bean
    fun awsS3Client(): S3Client =
        S3Client
            .builder()
            .region(Region.of(awsProperties.s3.region))
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build()
}
