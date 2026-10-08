package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BackendHealth
import com.example.local.AIRecord
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RoseCoral
import com.example.ui.theme.WarmAmber

@Composable
fun ServerStatusBanner(
    health: BackendHealth?,
    serverUrl: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOnline = health?.isOnline == true

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("server_status_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF1E293B)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    if (isOnline) NeonEmerald else CyberCyan.copy(alpha = 0.5f),
                    if (isOnline) CyberCyan else ElectricViolet.copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) NeonEmerald else WarmAmber)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isOnline) "Backend: Connected (${health?.latencyMs}ms)" else "Backend: Ready for DIG AI Server",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isOnline) NeonEmerald else Color.White
                    )
                    Text(
                        text = serverUrl,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("refresh_health_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh connection status",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ResultCard(
    record: AIRecord,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(CyberCyan.copy(alpha = 0.6f), ElectricViolet.copy(alpha = 0.6f))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeColor = when (record.taskType) {
                        "SUMMARY" -> CyberCyan
                        "SENTIMENT" -> if (record.sentimentLabel == "POSITIVE") NeonEmerald else if (record.sentimentLabel == "NEGATIVE") RoseCoral else WarmAmber
                        else -> ElectricViolet
                    }
                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = record.taskType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeColor
                        )
                    }
                    if (record.sentimentLabel != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = (if (record.sentimentLabel == "POSITIVE") NeonEmerald else if (record.sentimentLabel == "NEGATIVE") RoseCoral else WarmAmber).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = record.sentimentLabel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (record.sentimentLabel == "POSITIVE") NeonEmerald else if (record.sentimentLabel == "NEGATIVE") RoseCoral else WarmAmber
                            )
                        }
                    }
                }

                FilledTonalIconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(record.result))
                        copied = true
                    },
                    modifier = Modifier.size(36.dp).testTag("copy_result_button")
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                        contentDescription = "Copy AI response",
                        modifier = Modifier.size(16.dp),
                        tint = if (copied) NeonEmerald else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = record.result,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Model: ${record.modelName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${record.executionTimeMs} ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan
                )
            }
        }
    }
}
