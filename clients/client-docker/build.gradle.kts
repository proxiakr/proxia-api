val dockerJavaVersion: String by project

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.github.docker-java:docker-java-core:$dockerJavaVersion")
    implementation("com.github.docker-java:docker-java-transport-zerodep:$dockerJavaVersion")
}
