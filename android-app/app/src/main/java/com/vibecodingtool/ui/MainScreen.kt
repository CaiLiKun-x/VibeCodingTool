package com.vibecodingtool.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vibecodingtool.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val serverAddress by viewModel.serverAddress.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Show error dialog if there's an error
    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left column: Media + Volume
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Media Panel
            MediaPanel(
                mediaState = state.media,
                onPlay = { viewModel.mediaPlay() },
                onPause = { viewModel.mediaPause() },
                onNext = { viewModel.mediaNext() },
                onPrevious = { viewModel.mediaPrevious() },
                modifier = Modifier.weight(1f)
            )
            
            // Volume Panel
            VolumePanel(
                volumeState = state.volume,
                onVolumeChange = { viewModel.setVolume(it) },
                onToggleMute = { viewModel.toggleMute() },
                onVolumeUp = { viewModel.volumeUp() },
                onVolumeDown = { viewModel.volumeDown() },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Right column: VibeCoding + Connection
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // VibeCoding Panel
            VibeCodePanel(
                vibecodeState = state.vibecode,
                modifier = Modifier.weight(1f)
            )
            
            // Connection Panel
            ConnectionPanel(
                isConnected = isConnected,
                serverAddress = serverAddress,
                onServerAddressChange = { viewModel.updateServerAddress(it) },
                onConnect = { viewModel.connect() },
                onDisconnect = { viewModel.disconnect() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
