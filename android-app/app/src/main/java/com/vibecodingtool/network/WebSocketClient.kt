package com.vibecodingtool.network

import com.google.gson.Gson
import com.vibecodingtool.data.ClientCommand
import com.vibecodingtool.data.FullState
import com.vibecodingtool.data.ServerMessage
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.TimeUnit

class VibeCodingWebSocketClient(
    serverUri: String,
    private val onStateUpdate: (FullState) -> Unit,
    private val onConnectionChange: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) {
    private val gson = Gson()
    private var webSocket: WebSocketClient? = null
    private var isConnected = false
    private val uri = URI(serverUri)

    fun connect() {
        webSocket = object : WebSocketClient(uri) {
            override fun onOpen(handshake: ServerHandshake?) {
                isConnected = true
                onConnectionChange(true)
                requestState()
            }

            override fun onMessage(message: String?) {
                message?.let { handleMessage(it) }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                isConnected = false
                onConnectionChange(false)
            }

            override fun onError(ex: Exception?) {
                onError(ex?.message ?: "Unknown error")
            }
        }
        webSocket?.connect()
    }

    fun disconnect() {
        webSocket?.close()
        isConnected = false
    }

    fun isConnected(): Boolean = isConnected

    private fun handleMessage(message: String) {
        try {
            val serverMessage = gson.fromJson(message, ServerMessage::class.java)
            when (serverMessage.type) {
                "full_state" -> {
                    val state = gson.fromJson(
                        gson.toJson(serverMessage.data),
                        FullState::class.java
                    )
                    onStateUpdate(state)
                }
            }
        } catch (e: Exception) {
            onError("Failed to parse message: ${e.message}")
        }
    }

    fun requestState() {
        sendCommand(ClientCommand(action = "get_state"))
    }

    fun mediaPlay() {
        sendCommand(ClientCommand(action = "media_control", command = "play"))
    }

    fun mediaPause() {
        sendCommand(ClientCommand(action = "media_control", command = "pause"))
    }

    fun mediaNext() {
        sendCommand(ClientCommand(action = "media_control", command = "next"))
    }

    fun mediaPrevious() {
        sendCommand(ClientCommand(action = "media_control", command = "previous"))
    }

    fun setVolume(level: Int) {
        sendCommand(ClientCommand(action = "set_volume", level = level))
    }

    fun toggleMute() {
        sendCommand(ClientCommand(action = "toggle_mute"))
    }

    fun volumeUp(step: Int = 10) {
        sendCommand(ClientCommand(action = "volume_up", step = step))
    }

    fun volumeDown(step: Int = 10) {
        sendCommand(ClientCommand(action = "volume_down", step = step))
    }

    private fun sendCommand(command: ClientCommand) {
        if (!isConnected) {
            onError("Not connected to server")
            return
        }
        try {
            val json = gson.toJson(command)
            webSocket?.send(json)
        } catch (e: Exception) {
            onError("Failed to send command: ${e.message}")
        }
    }
}
