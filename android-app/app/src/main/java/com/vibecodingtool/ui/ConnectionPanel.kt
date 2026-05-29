package com.vibecodingtool.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibecodingtool.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionPanel(
    isConnected: Boolean,
    serverAddress: String,
    onServerAddressChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    connectionType: String = "websocket",
    onConnectionTypeChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 脉冲动画 - 连接状态
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    var showConnectionTypeMenu by remember { mutableStateOf(false) }
    
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
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isConnected) AppleGreen.copy(alpha = 0.15f)
                            else AppleRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "连接",
                        tint = if (isConnected) AppleGreen else AppleRed,
                        modifier = Modifier
                            .size(20.dp)
                            .then(
                                if (isConnected) Modifier.alpha(pulseAlpha) else Modifier
                            )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "连接",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 连接类型选择器 - 苹果风格
            ExposedDropdownMenuBox(
                expanded = showConnectionTypeMenu,
                onExpandedChange = { showConnectionTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = if (connectionType == "websocket") "WebSocket" else "ADB",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("连接类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showConnectionTypeMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !isConnected,
                    shape = RoundedCornerShape(12.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = showConnectionTypeMenu,
                    onDismissRequest = { showConnectionTypeMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("WebSocket") },
                        onClick = {
                            onConnectionTypeChange("websocket")
                            showConnectionTypeMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "WebSocket"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("ADB") },
                        onClick = {
                            onConnectionTypeChange("adb")
                            showConnectionTypeMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "ADB"
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 连接状态 - 苹果风格
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 状态图标
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (isConnected) AppleGreen.copy(alpha = 0.15f)
                            else AppleRed.copy(alpha = 0.15f)
                        )
                        .then(
                            if (isConnected) {
                                Modifier
                                    .scale(pulseScale)
                                    .alpha(pulseAlpha)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = "状态",
                        modifier = Modifier.size(36.dp),
                        tint = if (isConnected) AppleGreen else AppleRed
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = if (isConnected) "已连接" else "未连接",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isConnected) AppleGreen else AppleRed,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.then(
                        if (isConnected) Modifier.alpha(pulseAlpha) else Modifier
                    )
                )
                
                if (isConnected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (connectionType == "websocket") serverAddress else "ADB 连接",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 服务器地址输入（仅 WebSocket）
            if (connectionType == "websocket") {
                OutlinedTextField(
                    value = serverAddress,
                    onValueChange = onServerAddressChange,
                    label = { Text("服务器地址") },
                    placeholder = { Text("ws://192.168.1.100:8765") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isConnected,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "服务器"
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                // ADB 连接信息 - 苹果风格
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "ADB 连接",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "通过 USB 调试连接，请确保 ADB 已配置。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 连接/断开按钮 - 苹果风格
            Button(
                onClick = { if (isConnected) onDisconnect() else onConnect() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) AppleRed else AppleBlue,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                    contentDescription = if (isConnected) "断开连接" else "连接",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "断开连接" else "连接",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
