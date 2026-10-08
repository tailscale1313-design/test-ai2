package com.example.data

import com.example.local.AIRecord
import com.example.local.AIRecordDao
import com.example.network.RetrofitClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException

data class BackendHealth(
    val isOnline: Boolean,
    val message: String,
    val latencyMs: Long = 0L
)

class AIRepository(
    private val recordDao: AIRecordDao
) {
    val allRecords: Flow<List<AIRecord>> = recordDao.getAllRecords()

    suspend fun checkBackendHealth(customUrl: String? = null): BackendHealth = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val service = RetrofitClientProvider.getService(customUrl)
            val response = service.checkHealth()
            val latency = System.currentTimeMillis() - startTime
            if (response.isSuccessful) {
                val body = response.body()
                BackendHealth(
                    isOnline = true,
                    message = "${body?.service ?: "DIG AI Backend"}: ${body?.status ?: "Online"}",
                    latencyMs = latency
                )
            } else {
                BackendHealth(
                    isOnline = false,
                    message = "Server HTTP error ${response.code()}: ${response.message()}",
                    latencyMs = latency
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            BackendHealth(
                isOnline = false,
                message = e.localizedMessage ?: "Connection failed (Offline/Unreachable)",
                latencyMs = latency
            )
        }
    }

    suspend fun generatePrompt(
        prompt: String,
        taskType: String = "CHAT",
        maxTokens: Int = 1024,
        temperature: Double = 0.7,
        topK: Int = 50,
        customUrl: String? = null
    ): Result<AIRecord> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val request = PromptRequest(
            prompt = prompt,
            max_tokens = maxTokens,
            temperature = temperature,
            top_k = topK
        )

        try {
            val service = RetrofitClientProvider.getService(customUrl)
            val response = service.generateAiResponse(request)
            val elapsed = System.currentTimeMillis() - startTime

            if (response.isSuccessful && response.body() != null) {
                val aiResp = response.body()!!
                val sentiment = if (taskType == "SENTIMENT") parseSentiment(aiResp.generated_text) else null
                val record = AIRecord(
                    taskType = taskType,
                    prompt = prompt,
                    result = aiResp.generated_text,
                    modelName = aiResp.model_used ?: "Mistral-7B / Local Transformer",
                    executionTimeMs = elapsed,
                    sentimentScore = sentiment?.first,
                    sentimentLabel = sentiment?.second
                )
                val id = recordDao.insertRecord(record)
                Result.success(record.copy(id = id))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Result.failure(IOException("Server error: $errorMsg"))
            }
        } catch (e: Exception) {
            // If offline, provide intelligent offline heuristic fallback simulation
            // with prominent label so user has continuous functional testability
            val elapsed = System.currentTimeMillis() - startTime
            val fallbackResult = generateLocalOfflineFallback(prompt, taskType)
            val sentiment = if (taskType == "SENTIMENT") parseSentiment(fallbackResult) else null

            val fallbackRecord = AIRecord(
                taskType = taskType,
                prompt = prompt,
                result = fallbackResult,
                modelName = "Local Heuristic Engine (Server Offline: ${e.message ?: "Connect to backend"})",
                executionTimeMs = elapsed,
                sentimentScore = sentiment?.first,
                sentimentLabel = sentiment?.second
            )
            val id = recordDao.insertRecord(fallbackRecord)
            Result.success(fallbackRecord.copy(id = id))
        }
    }

    suspend fun deleteRecord(record: AIRecord) = withContext(Dispatchers.IO) {
        recordDao.deleteRecord(record)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        recordDao.clearAll()
    }

    private fun parseSentiment(text: String): Pair<Float, String> {
        val lower = text.lowercase()
        return when {
            lower.contains("positive") || lower.contains("optimistic") || lower.contains("delighted") || lower.contains("excellent") -> {
                0.85f to "POSITIVE"
            }
            lower.contains("negative") || lower.contains("frustrated") || lower.contains("unhappy") || lower.contains("terrible") || lower.contains("poor") -> {
                -0.75f to "NEGATIVE"
            }
            else -> {
                0.0f to "NEUTRAL"
            }
        }
    }

    private fun generateLocalOfflineFallback(prompt: String, taskType: String): String {
        return when (taskType) {
            "SUMMARY" -> {
                val clean = prompt.replace(Regex("""Summarize the following text.*?:""", RegexOption.IGNORE_CASE), "").trim()
                val sentences = clean.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
                if (sentences.isNotEmpty()) {
                    val keyPoints = sentences.take(3).joinToString("\n• ") { it.trim() }
                    "• Key Overview: $keyPoints\n\n[Summary generated via on-device NLP parser while connecting to FastAPI backend at ${RetrofitClientProvider.getCurrentBaseUrl()}]"
                } else {
                    "Summary: Concise condensation of input content.\n[Backend connection pending: start python main.py]"
                }
            }
            "SENTIMENT" -> {
                val lower = prompt.lowercase()
                val positiveKeywords = listOf("great", "love", "amazing", "happy", "excellent", "superb", "good", "fast", "innovative", "success", "pleasant", "best")
                val negativeKeywords = listOf("bad", "terrible", "awful", "hate", "slow", "broken", "bug", "crash", "worst", "fail", "angry", "poor", "difficult")

                val posMatches = positiveKeywords.count { lower.contains(it) }
                val negMatches = negativeKeywords.count { lower.contains(it) }

                val (label, score, breakdown) = when {
                    posMatches > negMatches -> Triple("POSITIVE", 0.88f, "Dominant positive polarity. Strong affirmative language detected.")
                    negMatches > posMatches -> Triple("NEGATIVE", -0.78f, "Dominant negative polarity. Frustration or critical friction detected.")
                    else -> Triple("NEUTRAL", 0.05f, "Balanced or objective tone without strong emotional polarization.")
                }

                """Sentiment: $label
Confidence Score: ${(score * 100).toInt()}%
Tone Analysis: $breakdown
Key Insights: Positives detected: $posMatches, Negatives detected: $negMatches.
[Analysis performed via local analyzer. Run FastAPI server for deep Mistral transformer embeddings.]""".trimIndent()
            }
            else -> {
                """[Local AI Assistant - Offline Mode]
Processed your request:
"${prompt.take(120)}${if (prompt.length > 120) "..." else ""}"

Your DIG AI FastAPI backend is currently unreachable at ${RetrofitClientProvider.getCurrentBaseUrl()}.
To run the full Mistral-7B backend:
1. Run `python main.py` or `uvicorn main:app --host 0.0.0.0 --port 8000`
2. Update the server URL in Settings (e.g. http://10.0.2.2:8000/ or LAN IP).""".trimIndent()
            }
        }
    }
}
