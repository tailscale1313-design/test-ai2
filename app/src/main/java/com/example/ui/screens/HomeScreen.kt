package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.AIViewModel
import com.example.ui.ActiveScreen
import com.example.ui.components.ServerStatusBanner
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RoseCoral
import com.example.ui.theme.WarmAmber

@Composable
fun HomeScreen(
    viewModel: AIViewModel,
    modifier: Modifier = Modifier
) {
    val backendHealth by viewModel.backendHealth.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val records by viewModel.recordsList.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.img_ai_hero_banner),
                    contentDescription = "AI Studio Neural Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFF0A0F1D).copy(alpha = 0.95f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Local AI Studio",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mistral-7B Neural Engine • Summarization & Sentiment",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Live Server Status Banner
        ServerStatusBanner(
            health = backendHealth,
            serverUrl = serverUrl,
            onRefresh = { viewModel.checkHealth() }
        )

        // Feature Action Hub
        Text(
            text = "AI Capabilities",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        FeatureHubCard(
            title = "Text Summarizer",
            subtitle = "Condense articles, meeting notes, reports, and text into key bullet points or executive TL;DR.",
            icon = Icons.Default.ShortText,
            accentColor = CyberCyan,
            onClick = { viewModel.setScreen(ActiveScreen.SUMMARIZER) },
            testTag = "home_card_summarizer"
        )

        FeatureHubCard(
            title = "Sentiment & Emotion Analysis",
            subtitle = "Extract polarity (Positive, Neutral, Negative), confidence score, emotional nuances, and takeaways.",
            icon = Icons.Default.Psychology,
            accentColor = ElectricViolet,
            onClick = { viewModel.setScreen(ActiveScreen.SENTIMENT) },
            testTag = "home_card_sentiment"
        )

        FeatureHubCard(
            title = "Prompt Playground",
            subtitle = "Execute unrestricted inference prompts with customizable Temperature, Max Tokens, and Top-K.",
            icon = Icons.Default.AutoAwesome,
            accentColor = NeonEmerald,
            onClick = { viewModel.setScreen(ActiveScreen.PROMPT_PLAYGROUND) },
            testTag = "home_card_playground"
        )

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.setScreen(ActiveScreen.HISTORY) }
                    .testTag("home_card_history"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "${records.size} Inferences", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "View History", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.setScreen(ActiveScreen.SERVER_SETTINGS) }
                    .testTag("home_card_settings"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Backend", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Configure IP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.5f), Color.Transparent))
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Navigate to $title",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
