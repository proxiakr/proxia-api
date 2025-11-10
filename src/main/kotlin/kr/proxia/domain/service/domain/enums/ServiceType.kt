package kr.proxia.domain.service.domain.enums

enum class ServiceType {
    APP, // 애플리케이션 (React, Vue, Spring Boot, Express 등)
    DATABASE, // 데이터베이스 (PostgreSQL, MySQL, MongoDB 등)
    CACHE, // 캐시 (Redis, Memcached 등)
    MESSAGE_QUEUE, // 메시지 큐 (RabbitMQ, Kafka 등)
    STORAGE, // 스토리지 (S3, MinIO 등)
    OTHER, // 기타
}
