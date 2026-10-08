package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.AIViewModel
import com.example.ui.GenerationState
import com.example.ui.components.ResultCard
import com.example.ui.components.ServerStatusBanner
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummarizerScreen(
    viewModel: AIViewModel,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val styles = listOf("Key Bullet Points", "Executive TL;DR", "Actionable Takeaways", "Single Paragraph")
    var selectedStyle by remember { mutableStateOf(styles[0]) }

    val generationState by viewModel.generationState.collectAsState()
    val backendHealth by viewModel.backendHealth.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val presetSamples = listOf(
        "AI Breakthrough" to "Researchers at Google and deep learning labs announced new architectural breakthroughs in generative multi-modal transformers that reduce inference memory overhead by 40% while preserving context fidelity across 1M+ token windows.",
        "Product Launch" to "We are thrilled to launch our next-generation developer platform. Featuring instant cold-start serverless runtimes, zero-latency edge caching across 35 regions, automated CI/CD canary deployments, and native real-time telemetry analytics.",
        "Meeting Notes" to "The quarterly engineering review focused on database scalability under high concurrency. Key decisions: migrate search indexing to specialized vectors, enforce zero-trust token rotations, and refactor slow blocking network requests."
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero visual card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.img_ai_hero_banner),
                    contentDescription = "Neural Network AI Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Neural Text Summarizer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyberCyan
                    )
                    Text(
                        text = "Condense long articles, reports, and notes using DIG AI / Mistral",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                }
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
                text = "Quick Presets:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetSamples.forEach { (title, sample) ->
                    SuggestionChip(
                        onClick = { textInput = sample },
                        label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("preset_$title")
                    )
                }
            }
        }

        // Summary Style Selection
        Column {
            Text(
                text = "Summary Format:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.forEach { style ->
                    val isSelected = selectedStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStyle = style },
                        label = { Text(style, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                            selectedLabelColor = CyberCyan
                        )
                    )
                }
            }
        }

        // Input Text Area
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Paste or type text to summarize...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("summarize_input_field"),
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

        // Summarize Button
        Button(
            onClick = {
                viewModel.summarizeText(textInput, selectedStyle)
            },
            enabled = textInput.isNotBlank() && generationState !is GenerationState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_summarize_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = androidx.compose.ui.graphics.Color.Black
            )
        ) {
            if (generationState is GenerationState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = androidx.compose.ui.graphics.Color.Black,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Synthesizing Summary...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Summary", fontWeight = FontWeight.Bold)
            }
        }

        // Output Result
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
