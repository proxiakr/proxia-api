package kr.proxia.global.container

import org.springframework.stereotype.Component

@Component
class DockerfileGenerator {
    fun generateSpringBootDockerfile(
        buildCommand: String?,
        startCommand: String?,
    ): String {
        val buildCommand = buildCommand ?: "./gradlew build"
        val startCommand = startCommand ?: "java -jar build/libs/*.jar"

        return """
            FROM eclipse-temurin:21-jdk AS builder
            WORKDIR /app
            COPY . .
            RUN $buildCommand
            
            FROM eclipse-temurin:21-jre
            WORKDIR /app
            COPY --from=builder /app/build/libs/*.jar app.jar
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }

    fun generateNodeJsDockerfile(
        installCommand: String?,
        buildCommand: String?,
        startCommand: String?,
    ): String {
        val installCommand = installCommand ?: "npm install"
        val buildCommand = buildCommand ?: "npm run build"
        val startCommand = startCommand ?: "npm start"

        return """
            FROM node:20-alpine
            WORKDIR /app
            COPY package*.json ./
            RUN $installCommand
            COPY . .
            RUN $buildCommand
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }

    fun generatePythonDockerfile(
        installCommand: String?,
        startCommand: String?,
    ): String {
        val installCommand = installCommand ?: "pip install -r requirements.txt"
        val startCommand = startCommand ?: "python app.py"

        return """
            FROM python:3.11-slim
            WORKDIR /app
            COPY requirements.txt .
            RUN $installCommand
            COPY . .
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }

    fun generateGoDockerfile(
        buildCommand: String?,
        startCommand: String?,
    ): String {
        val buildCommand = buildCommand ?: "go build -o app"
        val startCommand = startCommand ?: "./app"

        return """
            FROM golang:1.21-alpine AS builder
            WORKDIR /app
            COPY . .
            RUN $buildCommand
            FROM alpine:latest
            WORKDIR /app
            COPY --from=builder /app/app .
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }

    fun generateGenericDockerfile(startCommand: String?): String {
        val startCommand = startCommand ?: "echo 'No start command provided' && exit 1"

        return """
            FROM ubuntu:22.04
            WORKDIR /app
            COPY . .
            EXPOSE 8080
            CMD $startCommand
            """.trimIndent()
    }
}
