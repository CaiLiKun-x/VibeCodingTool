package com.vibecodingtool

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vibecodingtool.service.MediaPlaybackService
import com.vibecodingtool.ui.ConnectionSettingsScreen
import com.vibecodingtool.ui.MainScreen
import com.vibecodingtool.ui.SettingsScreen
import com.vibecodingtool.ui.theme.VibeCodingToolTheme
import com.vibecodingtool.viewmodel.MainViewModel
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "VibeCodingTool"
        private const val CRASH_LOG_FILE = "crash_log.txt"
    }
    
    private var mediaPlaybackService: MediaPlaybackService? = null
    private var isBound = false
    
    private fun getCrashLog(): String {
        return try {
            val file = File(filesDir, CRASH_LOG_FILE)
            if (file.exists()) file.readText() else ""
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun saveCrashLog(throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val log = "=== Crash at $timestamp ===\n$sw\n\n"
            File(filesDir, CRASH_LOG_FILE).appendText(log)
            Log.e(TAG, log)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash log", e)
        }
    }
    
    private fun clearCrashLog() {
        try {
            File(filesDir, CRASH_LOG_FILE).delete()
        } catch (e: Exception) {
            // ignore
        }
    }
    
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MediaPlaybackService.LocalBinder
            mediaPlaybackService = binder.getService()
            isBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            mediaPlaybackService = null
            isBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Global exception handler to prevent silent crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrashLog(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        enableEdgeToEdge()
        
        // Start and bind to media playback service
        val serviceIntent = Intent(this, MediaPlaybackService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        
        setContent {
            val viewModel: MainViewModel = viewModel()
            val darkTheme by viewModel.darkTheme.collectAsState()
            val showSettings by viewModel.showSettings.collectAsState()
            val showConnectionSettings by viewModel.showConnectionSettings.collectAsState()
            val autoConnect by viewModel.autoConnect.collectAsState()
            val serverAddress by viewModel.serverAddress.collectAsState()
            val isConnected by viewModel.isConnected.collectAsState()
            val connectionType by viewModel.connectionType.collectAsState()
            val state by viewModel.state.collectAsState()
            var crashLog by remember { mutableStateOf(getCrashLog()) }
            var showCrashDialog by remember { mutableStateOf(crashLog.isNotEmpty()) }
            
            // Update notification when playback state changes
            LaunchedEffect(state.media.playing) {
                if (isBound) {
                    mediaPlaybackService?.updateNotification(state.media.playing)
                }
            }
            
            VibeCodingToolTheme(darkTheme = darkTheme) {
                // Show crash dialog if there was a previous crash
                if (showCrashDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { 
                            showCrashDialog = false
                            clearCrashLog()
                        },
                        title = { androidx.compose.material3.Text("应用崩溃日志") },
                        text = { 
                            androidx.compose.material3.Text(
                                text = crashLog,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                        },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = { 
                                showCrashDialog = false
                                clearCrashLog()
                            }) {
                                androidx.compose.material3.Text("确定")
                            }
                        }
                    )
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        showSettings -> {
                            SettingsScreen(
                                autoConnect = autoConnect,
                                onAutoConnectChange = { viewModel.setAutoConnect(it) },
                                darkTheme = darkTheme,
                                onDarkThemeChange = { viewModel.setDarkTheme(it) },
                                serverAddress = serverAddress,
                                onServerAddressChange = { viewModel.updateServerAddress(it) },
                                onBack = { viewModel.toggleSettings() }
                            )
                        }
                        showConnectionSettings -> {
                            ConnectionSettingsScreen(
                                isConnected = isConnected,
                                serverAddress = serverAddress,
                                onServerAddressChange = { viewModel.updateServerAddress(it) },
                                onConnect = { viewModel.connect() },
                                onDisconnect = { viewModel.disconnect() },
                                connectionType = connectionType,
                                onConnectionTypeChange = { viewModel.setConnectionType(it) },
                                onBack = { viewModel.toggleConnectionSettings() }
                            )
                        }
                        else -> {
                            MainScreen(
                                onSettingsClick = { viewModel.toggleSettings() },
                                onConnectionSettingsClick = { viewModel.toggleConnectionSettings() }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
