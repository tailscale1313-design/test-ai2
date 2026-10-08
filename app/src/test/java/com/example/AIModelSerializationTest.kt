package com.example

import com.example.data.AIResponse
import com.example.data.PromptRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AIModelSerializationTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testPromptRequestSerialization() {
        val adapter = moshi.adapter(PromptRequest::class.java)
        val request = PromptRequest(
            prompt = "Summarize this article",
            max_tokens = 512,
            temperature = 0.7,
            top_k = 40
        )
        val json = adapter.toJson(request)
        assertNotNull(json)
        assert(json.contains("\"prompt\":\"Summarize this article\""))
        assert(json.contains("\"max_tokens\":512"))
        assert(json.contains("\"temperature\":0.7"))
        assert(json.contains("\"top_k\":40"))
    }

    @Test
    fun testAIResponseDeserialization() {
        val adapter = moshi.adapter(AIResponse::class.java)
        val json = """
            {
                "generated_text": "Here is the concise summary of your article.",
                "model_used": "mistralai/Mistral-7B-Instruct-v0.2"
            }
        """.trimIndent()
        val response = adapter.fromJson(json)
        assertNotNull(response)
        assertEquals("Here is the concise summary of your article.", response?.generated_text)
        assertEquals("mistralai/Mistral-7B-Instruct-v0.2", response?.model_used)
    }
}
