package com.vibecodingtool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibecodingtool.data.FullState
import com.vibecodingtool.data.MediaState
import com.vibecodingtool.data.VolumeState
import com.vibecodingtool.data.VibeCodeState
import com.vibecodingtool.network.VibeCodingWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(FullState())
    val state: StateFlow<FullState> = _state.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _serverAddress = MutableStateFlow("ws://192.168.1.100:8765")
    val serverAddress: StateFlow<String> = _serverAddress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var webSocketClient: VibeCodingWebSocketClient? = null

    fun updateServerAddress(address: String) {
        _serverAddress.value = address
    }

    fun connect() {
        disconnect()
        
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

    fun disconnect() {
        webSocketClient?.disconnect()
        webSocketClient = null
        _isConnected.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Media controls
    fun mediaPlay() {
        webSocketClient?.mediaPlay()
    }

    fun mediaPause() {
        webSocketClient?.mediaPause()
    }

    fun mediaNext() {
        webSocketClient?.mediaNext()
    }

    fun mediaPrevious() {
        webSocketClient?.mediaPrevious()
    }

    // Volume controls
    fun setVolume(level: Int) {
        webSocketClient?.setVolume(level)
    }

    fun toggleMute() {
        webSocketClient?.toggleMute()
    }

    fun volumeUp() {
        webSocketClient?.volumeUp()
    }

    fun volumeDown() {
        webSocketClient?.volumeDown()
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
