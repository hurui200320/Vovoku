package info.skyblond.vovoku.commons.models

data class UserLoginRequest(
    val username: String,
    val passwordHash: String
)