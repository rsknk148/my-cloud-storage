package com.cloudstorage

data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class CloudFile(
    val filename: String,
    val size: Long,
    val date: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginResponse(
    val user: User
)

data class FilesResponse(
    val files: List<CloudFile>
)

data class UploadResponse(
    val file: CloudFile
)