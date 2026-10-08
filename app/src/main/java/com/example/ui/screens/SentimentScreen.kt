package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AIViewModel
import com.example.ui.GenerationState
import com.example.ui.components.ResultCard
import com.example.ui.components.ServerStatusBanner
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RoseCoral
import com.example.ui.theme.WarmAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentimentScreen(
    viewModel: AIViewModel,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val generationState by viewModel.generationState.collectAsState()
    val backendHealth by viewModel.backendHealth.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val quickTestExamples = listOf(
        "Ecstatic Review" to "Absolutely thrilled with this service! The response time is instantaneous, UI is sublime, and the team solved my issue within 2 minutes.",
        "Frustrated Customer" to "Terrible experience. The system crashed twice, lost my unsaved draft, and customer support was completely unresponsive.",
        "Neutral Brief" to "The platform updated its privacy policy today to comply with regional data governance directives. No immediate user action is needed."
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sentiment Feature Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(ElectricViolet, CyberCyan))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sentiment & Emotion Intelligence",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Detect tone polarity, emotional nuance, confidence scores, and user intent via DIG AI transformer embeddings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ServerStatusBanner(
            health = backendHealth,
            serverUrl = serverUrl,
            onRefresh = { viewModel.checkHealth() }
        )

        // Presets Chips
        Column {
            Text(
                text = "Sample Inputs:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickTestExamples.forEach { (title, sample) ->
                    SuggestionChip(
                        onClick = { textInput = sample },
                        label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("sentiment_preset_$title")
                    )
                }
            }
        }

        // Input Text Area
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Enter customer review, feedback, or message...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .testTag("sentiment_input_field"),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (textInput.isNotEmpty()) {
                        IconButton(onClick = { textInput = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear text")
                        }
                    }
                    IconButton(
                        onClick = {
                            val text = clipboard.getText()?.text
                            if (!text.isNullOrBlank()) {
                                textInput = text
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste from clipboard")
                    }
                }
            }
        )

        // Quick Indicator Icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SentimentIndicatorPill(
                icon = Icons.Default.SentimentSatisfiedAlt,
                label = "Positive",
                color = NeonEmerald
            )
            SentimentIndicatorPill(
                icon = Icons.Default.SentimentNeutral,
                label = "Neutral",
                color = WarmAmber
            )
            SentimentIndicatorPill(
                icon = Icons.Default.SentimentDissatisfied,
                label = "Negative",
                color = RoseCoral
            )
        }

        // Analyze Button
        Button(
            onClick = {
                viewModel.analyzeSentiment(textInput)
            },
            enabled = textInput.isNotBlank() && generationState !is GenerationState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_sentiment_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricViolet,
                contentColor = Color.White
            )
        ) {
            if (generationState is GenerationState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Analyzing Polarity...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Run Sentiment Analysis", fontWeight = FontWeight.Bold)
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

@Composable
private fun SentimentIndicatorPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}
