package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request payload mirroring the user's FastAPI PromptRequest model:
 * class PromptRequest(BaseModel):
 *     prompt: str
 *     max_tokens: int = 2048
 *     temperature: float = 0.8
 *     top_k: int = 50
 */
@JsonClass(generateAdapter = true)
data class PromptRequest(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "max_tokens") val max_tokens: Int = 2048,
    @Json(name = "temperature") val temperature: Double = 0.8,
    @Json(name = "top_k") val top_k: Int = 50
)

/**
 * Response payload mirroring the user's FastAPI AIResponse model:
 * class AIResponse(BaseModel):
 *     generated_text: str
 *     model_used: str = MODEL_NAME
 */
@JsonClass(generateAdapter = true)
data class AIResponse(
    @Json(name = "generated_text") val generated_text: String,
    @Json(name = "model_used") val model_used: String? = null
)

/**
 * Health check root endpoint response:
 * {"status": "Server Running", "service": "DIG AI Backend"}
 */
@JsonClass(generateAdapter = true)
data class HealthResponse(
    @Json(name = "status") val status: String? = null,
    @Json(name = "service") val service: String? = null
)
