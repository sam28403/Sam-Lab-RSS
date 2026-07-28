package cc.samlab.rss.domain.repository

import cc.samlab.rss.infrastructure.net.ApiResult
import cc.samlab.rss.infrastructure.net.openai.OpenAiApiService
import cc.samlab.rss.infrastructure.net.openai.ChatCompletionRequest
import cc.samlab.rss.infrastructure.net.openai.ChatMessage
import cc.samlab.rss.infrastructure.net.openai.GeminiContent
import cc.samlab.rss.infrastructure.net.openai.GeminiGenerateContentRequest
import cc.samlab.rss.infrastructure.net.openai.GeminiPart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSummaryRepository @Inject constructor() {

    suspend fun fetchAvailableModels(
        baseUrl: String,
        apiKey: String
    ): ApiResult<List<String>> {
        return try {
            val service = OpenAiApiService.getInstance(baseUrl, apiKey)
            if (baseUrl.contains("googleapis.com")) {
                val response = service.getGeminiModels()
                if (response.isSuccessful && response.body() != null) {
                    val modelIds = response.body()!!.models.map { it.name.substringAfter("models/") }
                    ApiResult.Success(modelIds)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    ApiResult.BizError(Exception(errorMsg))
                }
            } else {
                val response = service.getModels()
                if (response.isSuccessful && response.body() != null) {
                    val modelIds = response.body()!!.data.map { it.id }
                    ApiResult.Success(modelIds)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    ApiResult.BizError(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }

    suspend fun summarizeArticle(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        articleContent: String
    ): ApiResult<String> {
        return try {
            val service = OpenAiApiService.getInstance(baseUrl, apiKey)

            if (baseUrl.contains("googleapis.com")) {
                val request = GeminiGenerateContentRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = "$prompt\n\n$articleContent")
                            )
                        )
                    )
                )
                val response = service.createGeminiContent(model, request)
                if (response.isSuccessful && response.body() != null) {
                    val candidates = response.body()!!.candidates
                    if (!candidates.isNullOrEmpty()) {
                        val summary = candidates[0].content.parts[0].text
                        ApiResult.Success(summary)
                    } else {
                        ApiResult.BizError(Exception("No candidates returned from API"))
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    ApiResult.BizError(Exception(errorMsg))
                }
            } else {
                val messages = listOf(
                    ChatMessage(role = "user", content = "$prompt\n\n$articleContent")
                )

                val request = ChatCompletionRequest(
                    model = model,
                    messages = messages,
                    temperature = 0.7,
                    maxTokens = 2000
                )

                val response = service.createChatCompletion(request)

                if (response.isSuccessful && response.body() != null) {
                    val choices = response.body()!!.choices
                    if (choices.isNotEmpty()) {
                        val summary = choices[0].message.content
                        ApiResult.Success(summary)
                    } else {
                        ApiResult.BizError(Exception("No choices returned from API"))
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    ApiResult.BizError(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }

    suspend fun chatWithAi(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>
    ): ApiResult<String> {
        return try {
            val service = OpenAiApiService.getInstance(baseUrl, apiKey)

            if (baseUrl.contains("googleapis.com")) {
                val geminiContents = messages.map { message ->
                    GeminiContent(
                        role = if (message.role == "assistant") "model" else "user",
                        parts = listOf(GeminiPart(text = message.content))
                    )
                }
                val request = GeminiGenerateContentRequest(contents = geminiContents)
                val response = service.createGeminiContent(model, request)
                if (response.isSuccessful && response.body() != null) {
                    val candidates = response.body()!!.candidates
                    if (!candidates.isNullOrEmpty()) {
                        val answer = candidates[0].content.parts[0].text
                        ApiResult.Success(answer)
                    } else {
                        ApiResult.BizError(Exception("No candidates returned from API"))
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    ApiResult.BizError(Exception(errorMsg))
                }
            } else {
                val request = ChatCompletionRequest(
                    model = model,
                    messages = messages,
                    temperature = 0.7,
                    maxTokens = 2000
                )

                val response = service.createChatCompletion(request)

                if (response.isSuccessful && response.body() != null) {
                    val choices = response.body()!!.choices
                    if (choices.isNotEmpty()) {
                        val answer = choices[0].message.content
                        ApiResult.Success(answer)
                    } else {
                        ApiResult.BizError(Exception("No choices returned from API"))
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    ApiResult.BizError(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }
}
