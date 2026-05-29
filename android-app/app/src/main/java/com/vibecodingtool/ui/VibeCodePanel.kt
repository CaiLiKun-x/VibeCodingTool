package com.vibecodingtool.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vibecodingtool.data.VibeCodeState
import com.vibecodingtool.data.VibeCodeToolState
import com.vibecodingtool.ui.theme.*

@Composable
fun VibeCodePanel(
    vibecodeState: VibeCodeState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "VibeCoding",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VibeCoding Status",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tool statuses
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolStatusCard(
                    name = "Codex",
                    icon = Icons.Default.SmartToy,
                    state = vibecodeState.codex
                )
                ToolStatusCard(
                    name = "Claude",
                    icon = Icons.Default.Psychology,
                    state = vibecodeState.claude
                )
                ToolStatusCard(
                    name = "OpenCode",
                    icon = Icons.Default.Terminal,
                    state = vibecodeState.opencode
                )
            }
        }
    }
}

@Composable
fun ToolStatusCard(
    name: String,
    icon: ImageVector,
    state: VibeCodeToolState,
    modifier: Modifier = Modifier
) {
    val statusColor = when (state.status) {
        "running" -> StatusGreen
        "waiting_input" -> StatusYellow
        "error" -> StatusRed
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    
    val statusText = when (state.status) {
        "running" -> "Running"
        "waiting_input" -> "NEED INPUT"
        "error" -> "Error"
        else -> "Inactive"
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (state.needsAttention) {
                StatusYellow.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(8.dp),
                            color = statusColor,
                            strokeWidth = 2.dp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
            
            if (state.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
            
            if (state.needsAttention) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Needs attention",
                        tint = StatusYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Requires your attention",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusYellow
                    )
                }
            }
        }
    }
}
