package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientProvider {

    // Default Android Emulator loopback is 10.0.2.2:8000
    // Real physical devices or LAN setups can use e.g. 192.168.1.X:8000
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8000/"
    const val DEFAULT_LOCALHOST_URL = "http://localhost:8000/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    @Volatile
    private var currentBaseUrl: String = DEFAULT_EMULATOR_URL

    @Volatile
    private var cachedService: AIService? = null

    fun getService(baseUrl: String? = null): AIService {
        val targetUrl = sanitizeUrl(baseUrl ?: currentBaseUrl)
        if (cachedService != null && targetUrl == currentBaseUrl) {
            return cachedService!!
        }

        synchronized(this) {
            currentBaseUrl = targetUrl
            val retrofit = Retrofit.Builder()
                .baseUrl(targetUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(AIService::class.java)
            cachedService = service
            return service
        }
    }

    fun updateBaseUrl(newUrl: String): AIService {
        val targetUrl = sanitizeUrl(newUrl)
        synchronized(this) {
            currentBaseUrl = targetUrl
            val retrofit = Retrofit.Builder()
                .baseUrl(targetUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val service = retrofit.create(AIService::class.java)
            cachedService = service
            return service
        }
    }

    fun getCurrentBaseUrl(): String = currentBaseUrl

    private fun sanitizeUrl(url: String): String {
        var clean = url.trim()
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "http://$clean"
        }
        if (!clean.endsWith("/")) {
            clean = "$clean/"
        }
        return clean
    }
}
