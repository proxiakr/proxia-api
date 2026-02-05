val awsS3Version: String by project

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("software.amazon.awssdk:s3:$awsS3Version")
}
