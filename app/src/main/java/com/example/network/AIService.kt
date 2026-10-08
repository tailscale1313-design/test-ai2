package com.example.network

import com.example.data.AIResponse
import com.example.data.HealthResponse
import com.example.data.PromptRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AIService {

    @GET("/")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("/api/generate")
    suspend fun generateAiResponse(@Body request: PromptRequest): Response<AIResponse>
}
