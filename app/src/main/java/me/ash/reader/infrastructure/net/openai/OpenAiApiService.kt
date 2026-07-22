package me.ash.reader.infrastructure.net.openai

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface OpenAiApiService {
    @GET("models")
    suspend fun getModels(): Response<ModelsResponse>

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    @GET("models")
    suspend fun getGeminiModels(): Response<GeminiModelsResponse>

    @POST("models/{model}:generateContent")
    suspend fun createGeminiContent(
        @Path("model") model: String,
        @Body request: GeminiGenerateContentRequest
    ): Response<GeminiGenerateContentResponse>

    companion object {
        fun getInstance(baseUrl: String, apiKey: String): OpenAiApiService {
            val authInterceptor = Interceptor { chain ->
                val originalRequest: Request = chain.request()
                val requestBuilder: Request.Builder = originalRequest.newBuilder()
                
                if (baseUrl.contains("googleapis.com")) {
                    requestBuilder.header("x-goog-api-key", apiKey)
                } else {
                    requestBuilder.header("Authorization", "Bearer $apiKey")
                }
                
                requestBuilder.header("Content-Type", "application/json")

                val request: Request = requestBuilder.build()
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenAiApiService::class.java)
        }
    }
}
