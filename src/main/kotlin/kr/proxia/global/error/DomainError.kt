package kr.proxia.global.error

import org.springframework.http.HttpStatus

interface DomainError {
    val status: HttpStatus
    val message: String
    val code: String
        get() {
            val prefix = this.javaClass.enclosingClass.simpleName
                .removeSuffix("Error")
                .toSnakeCase()
            val suffix = this.javaClass.simpleName.toSnakeCase()

            return "${prefix}_$suffix"
        }

    private fun String.toSnakeCase() = this.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").uppercase()
}
