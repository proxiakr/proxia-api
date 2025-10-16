package kr.proxia.global.security.oauth2.google.data

data class GoogleUserInfo(
    val sub: String,
    val email: String,
    val name: String,
    val picture: String
)