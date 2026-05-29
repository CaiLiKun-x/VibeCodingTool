package com.vibecodingtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vibecodingtool.ui.theme.*
import com.vibecodingtool.viewmodel.MainViewModel

@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    onConnectionSettingsClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    val padding = when {
        screenWidth < 600.dp -> 12.dp
        screenWidth < 840.dp -> 16.dp
        else -> 20.dp
    }
    
    val spacing = when {
        screenWidth < 600.dp -> 12.dp
        screenWidth < 840.dp -> 16.dp
        else -> 20.dp
    }
    
    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("错误") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLandscape || screenWidth > 840.dp) {
            // 横屏布局
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // 左侧：媒体面板（包含音量控制）
                MediaPanel(
                    mediaState = state.media,
                    volumeState = state.volume,
                    onPlay = { viewModel.mediaPlay() },
                    onPause = { viewModel.mediaPause() },
                    onNext = { viewModel.mediaNext() },
                    onPrevious = { viewModel.mediaPrevious() },
                    onVolumeChange = { viewModel.setVolume(it) },
                    onToggleMute = { viewModel.toggleMute() },
                    onVolumeUp = { viewModel.volumeUp() },
                    onVolumeDown = { viewModel.volumeDown() },
                    modifier = Modifier.weight(1f)
                )
                
                // 右侧：系统状态和 VibeCoding
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    // 系统状态面板
                    SystemStatusPanel(
                        systemState = state.system,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // VibeCoding 面板
                    VibeCodePanel(
                        vibecodeState = state.vibecode,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // 竖屏布局
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // 媒体面板（包含音量控制）
                MediaPanel(
                    mediaState = state.media,
                    volumeState = state.volume,
                    onPlay = { viewModel.mediaPlay() },
                    onPause = { viewModel.mediaPause() },
                    onNext = { viewModel.mediaNext() },
                    onPrevious = { viewModel.mediaPrevious() },
                    onVolumeChange = { viewModel.setVolume(it) },
                    onToggleMute = { viewModel.toggleMute() },
                    onVolumeUp = { viewModel.volumeUp() },
                    onVolumeDown = { viewModel.volumeDown() },
                    modifier = Modifier.weight(1f)
                )
                
                // 系统状态面板
                SystemStatusPanel(
                    systemState = state.system,
                    modifier = Modifier.weight(0.6f)
                )
                
                // VibeCoding 面板
                VibeCodePanel(
                    vibecodeState = state.vibecode,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // 顶部按钮栏
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 连接状态图标按钮
            FilledIconButton(
                onClick = onConnectionSettingsClick,
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isConnected) {
                        AppleGreen.copy(alpha = 0.2f)
                    } else {
                        AppleRed.copy(alpha = 0.2f)
                    },
                    contentColor = if (isConnected) AppleGreen else AppleRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = if (isConnected) "已连接" else "未连接",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 设置按钮
            FilledIconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
