package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AIViewModel
import com.example.ui.GenerationState
import com.example.ui.components.ResultCard
import com.example.ui.components.ServerStatusBanner
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonEmerald

@Composable
fun PlaygroundScreen(
    viewModel: AIViewModel,
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }
    var showParams by remember { mutableStateOf(false) }

    val generationState by viewModel.generationState.collectAsState()
    val backendHealth by viewModel.backendHealth.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    val temperature by viewModel.temperature.collectAsState()
    val maxTokens by viewModel.maxTokens.collectAsState()
    val topK by viewModel.topK.collectAsState()

    val scrollState = rememberScrollState()

    val promptIdeas = listOf(
        "Code Generation" to "Write a clean Kotlin coroutine function that performs exponential backoff retry on HTTP exceptions.",
        "Entity Extraction" to "Extract named entities (People, Organizations, Dates, Locations) from: 'Satya Nadella visited OpenAI headquarters in San Francisco on October 12th.'",
        "Creative Rewrite" to "Rewrite this corporate email in an engaging, visionary, modern tone: 'We have updated the sprint goals to align with Q4 deadlines.'"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ServerStatusBanner(
            health = backendHealth,
            serverUrl = serverUrl,
            onRefresh = { viewModel.checkHealth() }
        )

        // Ideas Chips
        Column {
            Text(
                text = "Fast Ideas:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                promptIdeas.forEach { (title, prompt) ->
                    SuggestionChip(
                        onClick = { promptInput = prompt },
                        label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("idea_$title")
                    )
                }
            }
        }

        // Prompt Input
        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            label = { Text("Enter custom prompt for Mistral-7B / Local AI...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .testTag("playground_input_field"),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                if (promptInput.isNotEmpty()) {
                    IconButton(onClick = { promptInput = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear prompt")
                    }
                }
            }
        )

        // Toggle Hyperparameters button
        OutlinedButton(
            onClick = { showParams = !showParams },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (showParams) "Hide Inference Hyperparameters" else "Adjust Hyperparameters (Temp: $temperature, Tokens: $maxTokens, Top-K: $topK)")
        }

        if (showParams) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Temperature: $temperature",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = temperature.toFloat(),
                        onValueChange = { viewModel.updateTemperature(it.toDouble()) },
                        valueRange = 0.1f..1.5f,
                        steps = 13,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Max Tokens: $maxTokens",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = maxTokens.toFloat(),
                        onValueChange = { viewModel.updateMaxTokens(it.toInt()) },
                        valueRange = 128f..2048f,
                        steps = 14,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Top-K: $topK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = topK.toFloat(),
                        onValueChange = { viewModel.updateTopK(it.toInt()) },
                        valueRange = 10f..100f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Run Prompt Button
        Button(
            onClick = {
                viewModel.runFreePrompt(promptInput)
            },
            enabled = promptInput.isNotBlank() && generationState !is GenerationState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_playground_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = Color.Black
            )
        ) {
            if (generationState is GenerationState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Inferring Response...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate AI Response", fontWeight = FontWeight.Bold)
            }
        }

        // Result Card
        AnimatedVisibility(visible = generationState is GenerationState.Success) {
            val state = generationState as? GenerationState.Success
            if (state != null) {
                ResultCard(record = state.record)
            }
        }

        AnimatedVisibility(visible = generationState is GenerationState.Error) {
            val errState = generationState as? GenerationState.Error
            if (errState != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "Error: ${errState.message}",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}
