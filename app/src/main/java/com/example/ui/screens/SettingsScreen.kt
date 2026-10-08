package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AIViewModel
import com.example.ui.components.ServerStatusBanner
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonEmerald

@Composable
fun SettingsScreen(
    viewModel: AIViewModel,
    modifier: Modifier = Modifier
) {
    val serverUrl by viewModel.serverUrl.collectAsState()
    val backendHealth by viewModel.backendHealth.collectAsState()
    var urlInput by remember(serverUrl) { mutableStateOf(serverUrl) }
    val clipboard = LocalClipboardManager.current
    var copiedBash by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val quickUrls = listOf(
        "Emulator Localhost" to "http://10.0.2.2:8000/",
        "Standard Localhost" to "http://localhost:8000/",
        "LAN IP Example" to "http://192.168.1.100:8000/"
    )

    val backendCodeSnippet = """
# Install FastAPI & HuggingFace Transformers
pip install fastapi uvicorn transformers torch accelerate

# Run the DIG AI FastAPI Server (main.py)
uvicorn main:app --host 0.0.0.0 --port 8000
    """.trimIndent()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DIG AI Backend Configuration",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        ServerStatusBanner(
            health = backendHealth,
            serverUrl = serverUrl,
            onRefresh = { viewModel.checkHealth() }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FastAPI Server URL",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Connects to POST /api/generate and GET / on your Python/Torch server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Base URL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("server_url_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.updateServerUrl(urlInput) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_server_url_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                    ) {
                        Text("Apply & Ping", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.checkHealth() }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickUrls.forEach { (label, url) ->
                        SuggestionChip(
                            onClick = {
                                urlInput = url
                                viewModel.updateServerUrl(url)
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Terminal Instructions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(CyberCyan, ElectricViolet)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FastAPI Host Command", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(backendCodeSnippet))
                            copiedBash = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (copiedBash) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = "Copy commands",
                            tint = if (copiedBash) NeonEmerald else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = backendCodeSnippet,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonEmerald
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Model: mistralai/Mistral-7B-Instruct-v0.2\n• Endpoint: POST /api/generate\n• Health Ping: GET /",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        }
    }
}
