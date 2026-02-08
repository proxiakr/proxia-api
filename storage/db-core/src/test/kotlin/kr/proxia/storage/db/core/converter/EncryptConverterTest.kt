package kr.proxia.storage.db.core.converter

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec

class EncryptConverterTest :
    FunSpec({
        val converter = EncryptConverter()

        beforeSpec {
            val keyBytes = MessageDigest.getInstance("SHA-256").digest("test-key".toByteArray())
            EncryptConverter.key = SecretKeySpec(keyBytes, "AES")
        }

        test("encrypt and decrypt returns original value") {
            val original = "my-secret-password"
            val encrypted = converter.convertToDatabaseColumn(original)
            val decrypted = converter.convertToEntityAttribute(encrypted)

            decrypted shouldBe original
        }

        test("null input returns null on encrypt") {
            converter.convertToDatabaseColumn(null) shouldBe null
        }

        test("null input returns null on decrypt") {
            converter.convertToEntityAttribute(null) shouldBe null
        }

        test("same value produces different ciphertext each time") {
            val original = "same-value"
            val encrypted1 = converter.convertToDatabaseColumn(original)
            val encrypted2 = converter.convertToDatabaseColumn(original)

            encrypted1 shouldNotBe encrypted2
        }

        test("tampered ciphertext fails decryption") {
            val encrypted = converter.convertToDatabaseColumn("secret")!!
            val tampered = encrypted.dropLast(1) + if (encrypted.last() == 'A') 'B' else 'A'

            shouldThrow<Exception> {
                converter.convertToEntityAttribute(tampered)
            }
        }

        test("empty string encrypts and decrypts correctly") {
            val encrypted = converter.convertToDatabaseColumn("")
            converter.convertToEntityAttribute(encrypted) shouldBe ""
        }

        test("long value encrypts and decrypts correctly") {
            val original = "a".repeat(10000)
            val encrypted = converter.convertToDatabaseColumn(original)
            converter.convertToEntityAttribute(encrypted) shouldBe original
        }

        test("unicode value encrypts and decrypts correctly") {
            val original = "비밀번호"
            val encrypted = converter.convertToDatabaseColumn(original)
            converter.convertToEntityAttribute(encrypted) shouldBe original
        }
    })
