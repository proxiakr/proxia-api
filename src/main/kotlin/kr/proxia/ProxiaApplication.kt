package kr.proxia

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProxiaApplication

fun main(args: Array<String>) {
    runApplication<ProxiaApplication>(*args)
}
