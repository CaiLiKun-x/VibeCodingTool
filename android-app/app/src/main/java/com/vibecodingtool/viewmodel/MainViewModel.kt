package com.vibecodingtool.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vibecodingtool.data.FullState
import com.vibecodingtool.data.MediaState
import com.vibecodingtool.data.VolumeState
import com.vibecodingtool.data.VibeCodeState
import com.vibecodingtool.network.AdbClient
import com.vibecodingtool.network.VibeCodingWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        "vibecoding_prefs", Context.MODE_PRIVATE
    )
    
    private val _state = MutableStateFlow(FullState())
    val state: StateFlow<FullState> = _state.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _serverAddress = MutableStateFlow(
        sharedPreferences.getString("server_address", "ws://192.168.1.100:8765") ?: "ws://192.168.1.100:8765"
    )
    val serverAddress: StateFlow<String> = _serverAddress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Settings states
    private val _autoConnect = MutableStateFlow(
        sharedPreferences.getBoolean("auto_connect", false)
    )
    val autoConnect: StateFlow<Boolean> = _autoConnect.asStateFlow()
    
    private val _darkTheme = MutableStateFlow(
        sharedPreferences.getBoolean("dark_theme", true)
    )
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()
    
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()
    
    private val _showConnectionSettings = MutableStateFlow(false)
    val showConnectionSettings: StateFlow<Boolean> = _showConnectionSettings.asStateFlow()
    
    // Connection type state
    private val _connectionType = MutableStateFlow(
        sharedPreferences.getString("connection_type", "websocket") ?: "websocket"
    )
    val connectionType: StateFlow<String> = _connectionType.asStateFlow()

    private var webSocketClient: VibeCodingWebSocketClient? = null
    private var adbClient: AdbClient? = null
    
    init {
        // Auto-connect if enabled
        if (_autoConnect.value) {
            connect()
        }
    }

    fun updateServerAddress(address: String) {
        _serverAddress.value = address
        sharedPreferences.edit().putString("server_address", address).apply()
    }
    
    fun setAutoConnect(enabled: Boolean) {
        _autoConnect.value = enabled
        sharedPreferences.edit().putBoolean("auto_connect", enabled).apply()
    }
    
    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        sharedPreferences.edit().putBoolean("dark_theme", enabled).apply()
    }
    
    fun setConnectionType(type: String) {
        _connectionType.value = type
        sharedPreferences.edit().putString("connection_type", type).apply()
    }
    
    fun toggleSettings() {
        _showSettings.value = !_showSettings.value
    }
    
    fun toggleConnectionSettings() {
        _showConnectionSettings.value = !_showConnectionSettings.value
    }

    fun connect() {
        disconnect()
        
        when (_connectionType.value) {
            "websocket" -> connectWebSocket()
            "adb" -> connectAdb()
            else -> connectWebSocket()
        }
    }
    
    private fun connectWebSocket() {
        webSocketClient = VibeCodingWebSocketClient(
            serverUri = _serverAddress.value,
            onStateUpdate = { state ->
                viewModelScope.launch {
                    _state.value = state
                }
            },
            onConnectionChange = { connected ->
                viewModelScope.launch {
                    _isConnected.value = connected
                }
            },
            onError = { error ->
                viewModelScope.launch {
                    _errorMessage.value = error
                }
            }
        )
        webSocketClient?.connect()
    }
    
    private fun connectAdb() {
        adbClient = AdbClient(
            context = getApplication(),
            onStateUpdate = { message ->
                viewModelScope.launch {
                    // Parse ADB response and update state
                    // This is a simplified implementation
                    try {
                        // In a real implementation, you would parse the JSON response
                        // and update the state accordingly
                        println("ADB Response: $message")
                    } catch (e: Exception) {
                        _errorMessage.value = "Failed to parse ADB response: ${e.message}"
                    }
                }
            },
            onConnectionChange = { connected ->
                viewModelScope.launch {
                    _isConnected.value = connected
                }
            },
            onError = { error ->
                viewModelScope.launch {
                    _errorMessage.value = error
                }
            }
        )
        
        // Extract host and port from server address
        // For ADB, we might use a different format or default values
        val host = "localhost"
        val port = 5555
        adbClient?.connect(host, port)
    }

    fun disconnect() {
        webSocketClient?.disconnect()
        webSocketClient = null
        
        adbClient?.disconnect()
        adbClient = null
        
        _isConnected.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Media controls
    fun mediaPlay() {
        when (_connectionType.value) {
            "websocket" -> webSocketClient?.mediaPlay()
            "adb" -> adbClient?.sendCommand("media_play")
        }
    }

    fun mediaPause() {
        when (_connectionType.value) {
            "websocket" -> webSocketClient?.mediaPause()
            "adb" -> adbClient?.sendCommand("media_pause")
        }
    }

    fun mediaNext() {
        when (_connectionType.value) {
            "websocket" -> webSocketClient?.mediaNext()
            "adb" -> adbClient?.sendCommand("media_next")
        }
    }

    fun mediaPrevious() {
        when (_connectionType.value) {
            "websocket" -> webSocketClient?.mediaPrevious()
            "adb" -> adbClient?.sendCommand("media_previous")
        }
    }

    // Volume controls
    fun setVolume(level: Int) {
        when (_connectionType.value) {
            "websocket" -> webSocketClient?.setVolume(level)
            "adb" -> adbClient?.sendCommand("set_volume $level")
        }
    }

    fun toggleMute() {
        when (_connectionType.value) {
            "websocket" -> webSocketClient?.toggleMute()
            "adb" -> adbClient?.sendCommand("toggle_mute")
        }
    }

    fun volumeUp() {
        when (_connectionType.value) {
            "websocket" -> webSocketClient?.volumeUp()
            "adb" -> adbClient?.sendCommand("volume_up")
        }
    }

    fun volumeDown() {
        when (_connectionType.value) {
            "websocket" -> webSocketClient?.volumeDown()
            "adb" -> adbClient?.sendCommand("volume_down")
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
