package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AIRepository
import com.example.data.BackendHealth
import com.example.local.AIRecord
import com.example.local.AppDatabase
import com.example.network.RetrofitClientProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class GenerationState {
    object Idle : GenerationState()
    object Loading : GenerationState()
    data class Success(val record: AIRecord) : GenerationState()
    data class Error(val message: String) : GenerationState()
}

enum class ActiveScreen {
    HOME,
    SUMMARIZER,
    SENTIMENT,
    PROMPT_PLAYGROUND,
    HISTORY,
    SERVER_SETTINGS
}

class AIViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AIRepository(database.aiRecordDao())

    val recordsList: StateFlow<List<AIRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentScreen = MutableStateFlow(ActiveScreen.HOME)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    private val _generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    private val _backendHealth = MutableStateFlow<BackendHealth?>(null)
    val backendHealth: StateFlow<BackendHealth?> = _backendHealth.asStateFlow()

    private val _serverUrl = MutableStateFlow(RetrofitClientProvider.getCurrentBaseUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    // Config parameters
    private val _temperature = MutableStateFlow(0.7)
    val temperature: StateFlow<Double> = _temperature.asStateFlow()

    private val _maxTokens = MutableStateFlow(1024)
    val maxTokens: StateFlow<Int> = _maxTokens.asStateFlow()

    private val _topK = MutableStateFlow(50)
    val topK: StateFlow<Int> = _topK.asStateFlow()

    init {
        checkHealth()
    }

    fun setScreen(screen: ActiveScreen) {
        _currentScreen.value = screen
    }

    fun updateTemperature(temp: Double) {
        _temperature.value = (temp * 10).toInt() / 10.0
    }

    fun updateMaxTokens(tokens: Int) {
        _maxTokens.value = tokens
    }

    fun updateTopK(k: Int) {
        _topK.value = k
    }

    fun updateServerUrl(newUrl: String) {
        _serverUrl.value = newUrl
        RetrofitClientProvider.updateBaseUrl(newUrl)
        checkHealth()
    }

    fun checkHealth() {
        viewModelScope.launch {
            _backendHealth.value = repository.checkBackendHealth(_serverUrl.value)
        }
    }

    fun summarizeText(inputText: String, summaryStyle: String = "Key Bullet Points") {
        if (inputText.isBlank()) return
        val structuredPrompt = """
[Task: Text Summarization]
Format requested: $summaryStyle
Please generate a clear, comprehensive, and accurate summary of the following content:

--- CONTENT START ---
$inputText
--- CONTENT END ---

Summary:
        """.trimIndent()

        executeGeneration(
            prompt = structuredPrompt,
            taskType = "SUMMARY"
        )
    }

    fun analyzeSentiment(inputText: String) {
        if (inputText.isBlank()) return
        val structuredPrompt = """
[Task: Sentiment Analysis]
Analyze the emotional tone and sentiment polarity of the following text:

--- TEXT ---
$inputText
--- END ---

Please provide:
1. Overall Sentiment Classification: (POSITIVE, NEGATIVE, or NEUTRAL)
2. Sentiment Score / Confidence: (e.g. 0.0 to 1.0)
3. Key Emotional Drivers & Nuance Breakdown:
4. Strategic Takeaway / Actionable Insight:
        """.trimIndent()

        executeGeneration(
            prompt = structuredPrompt,
            taskType = "SENTIMENT"
        )
    }

    fun runFreePrompt(prompt: String) {
        if (prompt.isBlank()) return
        executeGeneration(
            prompt = prompt,
            taskType = "CHAT"
        )
    }

    private fun executeGeneration(prompt: String, taskType: String) {
        viewModelScope.launch {
            _generationState.value = GenerationState.Loading
            val result = repository.generatePrompt(
                prompt = prompt,
                taskType = taskType,
                maxTokens = _maxTokens.value,
                temperature = _temperature.value,
                topK = _topK.value,
                customUrl = _serverUrl.value
            )
            result.onSuccess { record ->
                _generationState.value = GenerationState.Success(record)
            }.onFailure { err ->
                _generationState.value = GenerationState.Error(err.message ?: "Generation failed")
            }
        }
    }

    fun resetState() {
        _generationState.value = GenerationState.Idle
    }

    fun deleteRecord(record: AIRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
