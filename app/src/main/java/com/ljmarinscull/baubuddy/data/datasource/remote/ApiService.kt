package com.ljmarinscull.baubuddy.data.datasource.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.ljmarinscull.baubuddy.data.models.RemoteResourceJO
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@Serializable
data class UserCredentials(
    val username: String,
    val password: String
)

@Serializable
data class OAuthJO(val access_token: String, val token_type: String)

@Serializable
data class LoginResponse(val oauth: OAuthJO){
    val authorization = "${oauth.token_type} ${oauth.access_token}"
}

interface ApiService {
    @GET("/dev/index.php/v1/tasks/select")
    suspend fun requestResources(@Header("Authorization") authorization: String): List<RemoteResourceJO>

    @Headers(
        "Authorization: Basic QVBJX0V4cGxvcmVyOjEyMzQ1NmlzQUxhbWVQYXNz",
        "Content-Type: application/json"
    )
    @POST("/index.php/login")
    suspend fun login(@Body userCredentials: UserCredentials): LoginResponse
}

class RetrofitClient {

    companion object {
        @Volatile private var instance: ApiService? = null

        @Synchronized
        fun getInstance(): ApiService {

            if (instance == null) {
                val client = OkHttpClient()
                    .newBuilder()
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()

                val json = Json{
                    ignoreUnknownKeys = true
                    isLenient = true
                }
                instance = Retrofit.Builder()
                    .addConverterFactory(json.asConverterFactory(MediaType.parse("application/json")!!))
                    .baseUrl("https://api.baubuddy.de")
                    .client(client)
                    .build()
                    .create(ApiService::class.java)
            }
            return instance as ApiService
        }
    }
}