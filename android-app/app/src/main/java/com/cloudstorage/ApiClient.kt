package com.cloudstorage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient(context: Context) {
    
    private val preferenceManager = PreferenceManager(context)
    private val gson = Gson()
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .cookieJar(object : CookieJar {
            private val cookieStore = mutableMapOf<String, List<Cookie>>()
            
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }
            
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: listOf()
            }
        })
        .build()
    
    private val baseUrl: String
        get() = preferenceManager.serverUrl
    
    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val loginRequest = LoginRequest(username, password)
            val body = gson.toJson(loginRequest).toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$baseUrl/api/login")
                .post(body)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val apiResponse = gson.fromJson(responseBody, object : TypeToken<ApiResponse<LoginResponse>>() {}.type) as ApiResponse<LoginResponse>
                if (apiResponse.success && apiResponse.data != null) {
                    preferenceManager.saveUser(apiResponse.data.user)
                    Result.success(apiResponse.data.user)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Login failed"))
                }
            } else {
                val errorResponse = try {
                    gson.fromJson(responseBody, ApiResponse::class.java)
                } catch (e: JsonSyntaxException) {
                    null
                }
                Result.failure(Exception(errorResponse?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(username: String, email: String, password: String): Result<String> {
        return try {
            val registerRequest = RegisterRequest(username, email, password)
            val body = gson.toJson(registerRequest).toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$baseUrl/api/register")
                .post(body)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)
                if (apiResponse.success) {
                    Result.success(apiResponse.message ?: "Registration successful")
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Registration failed"))
                }
            } else {
                val errorResponse = try {
                    gson.fromJson(responseBody, ApiResponse::class.java)
                } catch (e: JsonSyntaxException) {
                    null
                }
                Result.failure(Exception(errorResponse?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFiles(): Result<List<CloudFile>> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/api/files")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val apiResponse = gson.fromJson(responseBody, object : TypeToken<ApiResponse<FilesResponse>>() {}.type) as ApiResponse<FilesResponse>
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data.files)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch files"))
                }
            } else {
                Result.failure(Exception("Failed to fetch files"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadFile(file: File): Result<CloudFile> {
        return try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("application/octet-stream".toMediaType())
                )
                .build()
            
            val request = Request.Builder()
                .url("$baseUrl/api/upload")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val apiResponse = gson.fromJson(responseBody, object : TypeToken<ApiResponse<UploadResponse>>() {}.type) as ApiResponse<UploadResponse>
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data.file)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Upload failed"))
                }
            } else {
                val errorResponse = try {
                    gson.fromJson(responseBody, ApiResponse::class.java)
                } catch (e: JsonSyntaxException) {
                    null
                }
                Result.failure(Exception(errorResponse?.message ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun downloadFile(filename: String): Result<ResponseBody> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/download/$filename")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful && response.body != null) {
                Result.success(response.body!!)
            } else {
                Result.failure(Exception("Download failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteFile(filename: String): Result<String> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/api/delete/$filename")
                .delete()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)
                if (apiResponse.success) {
                    Result.success(apiResponse.message ?: "File deleted successfully")
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Delete failed"))
                }
            } else {
                val errorResponse = try {
                    gson.fromJson(responseBody, ApiResponse::class.java)
                } catch (e: JsonSyntaxException) {
                    null
                }
                Result.failure(Exception(errorResponse?.message ?: "Delete failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}