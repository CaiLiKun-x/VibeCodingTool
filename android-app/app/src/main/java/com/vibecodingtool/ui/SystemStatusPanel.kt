package com.vibecodingtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibecodingtool.data.SystemState
import com.vibecodingtool.ui.theme.*

@Composable
fun SystemStatusPanel(
    systemState: SystemState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = "系统状态",
                    tint = AppleBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "系统状态",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 系统状态卡片
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CPU 使用率
                SystemStatusCard(
                    icon = Icons.Default.Memory,
                    title = "CPU",
                    value = "${systemState.cpu.usage.toInt()}%",
                    subtitle = "${systemState.cpu.coreCount} 核心",
                    progress = systemState.cpu.usage / 100f,
                    color = when {
                        systemState.cpu.usage > 80 -> AppleRed
                        systemState.cpu.usage > 60 -> AppleOrange
                        else -> AppleGreen
                    }
                )
                
                // 内存使用率
                SystemStatusCard(
                    icon = Icons.Default.Storage,
                    title = "内存",
                    value = "${systemState.memory.percentage.toInt()}%",
                    subtitle = formatBytes(systemState.memory.used) + " / " + formatBytes(systemState.memory.total),
                    progress = systemState.memory.percentage / 100f,
                    color = when {
                        systemState.memory.percentage > 80 -> AppleRed
                        systemState.memory.percentage > 60 -> AppleOrange
                        else -> AppleBlue
                    }
                )
                
                // GPU 使用率
                if (systemState.gpu.model.isNotEmpty() && systemState.gpu.model != "Unknown") {
                    SystemStatusCard(
                        icon = Icons.Default.VideoSettings,
                        title = "GPU",
                        value = if (systemState.gpu.usage > 0) "${systemState.gpu.usage.toInt()}%" else "N/A",
                        subtitle = systemState.gpu.model,
                        progress = systemState.gpu.usage / 100f,
                        color = when {
                            systemState.gpu.usage > 80 -> AppleRed
                            systemState.gpu.usage > 60 -> AppleOrange
                            else -> ApplePurple
                        }
                    )
                }
                
                // CPU 温度
                if (systemState.temperature.cpu > 0) {
                    SystemStatusCard(
                        icon = Icons.Default.Thermostat,
                        title = "温度",
                        value = "${systemState.temperature.cpu.toInt()}°C",
                        subtitle = if (systemState.temperature.gpu > 0) "GPU: ${systemState.temperature.gpu.toInt()}°C" else "CPU 温度",
                        progress = systemState.temperature.cpu / 100f,
                        color = when {
                            systemState.temperature.cpu > 80 -> AppleRed
                            systemState.temperature.cpu > 60 -> AppleOrange
                            else -> AppleGreen
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemStatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1073741824 -> "%.1f GB".format(bytes / 1073741824.0)
        bytes >= 1048576 -> "%.1f MB".format(bytes / 1048576.0)
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
