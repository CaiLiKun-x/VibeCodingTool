package com.vibecodingtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibecodingtool.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSettingsScreen(
    isConnected: Boolean,
    serverAddress: String,
    onServerAddressChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    connectionType: String,
    onConnectionTypeChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConnectionTypeMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "连接设置",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = AppleBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // 连接状态
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isConnected) AppleGreen.copy(alpha = 0.1f) else AppleRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isConnected) AppleGreen.copy(alpha = 0.2f)
                                    else AppleRed.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = "状态",
                                tint = if (isConnected) AppleGreen else AppleRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (isConnected) "已连接" else "未连接",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isConnected) AppleGreen else AppleRed,
                                fontWeight = FontWeight.Medium
                            )
                            if (isConnected) {
                                Text(
                                    text = serverAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // 连接类型选择
                Text(
                    text = "连接类型",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppleBlue,
                    fontWeight = FontWeight.SemiBold
                )
                
                ExposedDropdownMenuBox(
                    expanded = showConnectionTypeMenu,
                    onExpandedChange = { showConnectionTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = if (connectionType == "websocket") "WebSocket" else "ADB",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("选择连接方式") },
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
                
                // 服务器地址输入（仅 WebSocket）
                if (connectionType == "websocket") {
                    Text(
                        text = "服务器地址",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppleBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = serverAddress,
                        onValueChange = onServerAddressChange,
                        label = { Text("WebSocket 服务器地址") },
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
                    // ADB 连接信息
                    Text(
                        text = "ADB 连接",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppleBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    
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
                                text = "通过 USB 调试连接",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "请确保：",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "1. Android 设备已启用 USB 调试",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "2. ADB 已正确安装并配置",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "3. 设备已通过 USB 连接到电脑",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 连接/断开按钮
                Button(
                    onClick = { if (isConnected) onDisconnect() else onConnect() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) AppleRed else AppleBlue,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                        contentDescription = if (isConnected) "断开连接" else "连接",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isConnected) "断开连接" else "连接",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
