package com.vibecodingtool.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

class AdbClient(
    private val context: Context,
    private val onStateUpdate: (String) -> Unit,
    private val onConnectionChange: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) {
    private var socket: Socket? = null
    private var inputStream: BufferedReader? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false
    private var readingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val TAG = "AdbClient"
        private const val DEFAULT_PORT = 5555
    }
    
    fun connect(host: String, port: Int = DEFAULT_PORT) {
        scope.launch {
            try {
                // First, set up ADB port forwarding
                setupPortForwarding(host, port)
                
                // Then connect to the forwarded port
                socket = Socket("127.0.0.1", port)
                inputStream = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                outputStream = socket!!.getOutputStream()
                
                isConnected = true
                withContext(Dispatchers.Main) {
                    onConnectionChange(true)
                }
                
                // Start reading responses
                startReading()
                
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
                withContext(Dispatchers.Main) {
                    onError("ADB connection failed: ${e.message}")
                }
                disconnect()
            }
        }
    }
    
    private suspend fun setupPortForwarding(host: String, port: Int) {
        try {
            // Forward port from device to host
            val process = Runtime.getRuntime().exec(
                "adb forward tcp:$port tcp:$port"
            )
            process.waitFor()
            
            if (process.exitValue() != 0) {
                val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
                throw Exception("ADB forward failed: $error")
            }
            
            Log.d(TAG, "Port forwarding set up: tcp:$port -> tcp:$port")
            
        } catch (e: Exception) {
            Log.e(TAG, "Port forwarding failed", e)
            throw e
        }
    }
    
    private fun startReading() {
        readingJob = scope.launch {
            try {
                while (isConnected && socket?.isConnected == true) {
                    val line = inputStream?.readLine()
                    if (line != null) {
                        withContext(Dispatchers.Main) {
                            onStateUpdate(line)
                        }
                    } else {
                        // Connection closed
                        break
                    }
                }
            } catch (e: Exception) {
                if (isConnected) {
                    Log.e(TAG, "Reading error", e)
                    withContext(Dispatchers.Main) {
                        onError("Connection lost: ${e.message}")
                    }
                }
            } finally {
                disconnect()
            }
        }
    }
    
    fun sendCommand(command: String) {
        if (!isConnected) {
            onError("Not connected")
            return
        }
        
        scope.launch {
            try {
                outputStream?.write("$command\n".toByteArray())
                outputStream?.flush()
            } catch (e: Exception) {
                Log.e(TAG, "Send failed", e)
                withContext(Dispatchers.Main) {
                    onError("Failed to send command: ${e.message}")
                }
            }
        }
    }
    
    fun disconnect() {
        isConnected = false
        readingJob?.cancel()
        
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing connection", e)
        }
        
        inputStream = null
        outputStream = null
        socket = null
        
        scope.launch {
            withContext(Dispatchers.Main) {
                onConnectionChange(false)
            }
        }
    }
    
    fun isConnected(): Boolean = isConnected
    
    fun destroy() {
        disconnect()
        scope.cancel()
    }
}