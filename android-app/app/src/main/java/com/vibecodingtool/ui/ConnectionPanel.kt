package com.vibecodingtool.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibecodingtool.ui.theme.StatusGreen
import com.vibecodingtool.ui.theme.StatusRed

@Composable
fun ConnectionPanel(
    isConnected: Boolean,
    serverAddress: String,
    onServerAddressChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = "Connection",
                    tint = if (isConnected) StatusGreen else StatusRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connection",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Connection status
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status indicator
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = "Status",
                    modifier = Modifier.size(48.dp),
                    tint = if (isConnected) StatusGreen else StatusRed
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isConnected) "Connected" else "Disconnected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isConnected) StatusGreen else StatusRed
                )
                
                if (isConnected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = serverAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Server address input
            OutlinedTextField(
                value = serverAddress,
                onValueChange = onServerAddressChange,
                label = { Text("Server Address") },
                placeholder = { Text("ws://192.168.1.100:8765") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isConnected,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Server"
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Connect/Disconnect button
            Button(
                onClick = { if (isConnected) onDisconnect() else onConnect() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                    contentDescription = if (isConnected) "Disconnect" else "Connect"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "Disconnect" else "Connect"
                )
            }
        }
    }
}
