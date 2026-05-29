package com.vibecodingtool.data

import com.google.gson.annotations.SerializedName

data class MediaState(
    val playing: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val position: Double = 0.0,
    val duration: Double = 0.0,
    val app: String = "none",
    val artwork: String = "",  // Base64 encoded album art
    val lyrics: String = ""   // Current lyrics
)

data class VolumeState(
    val level: Int = 0,
    val muted: Boolean = false
)

data class VibeCodeToolState(
    val status: String = "inactive",
    val running: Boolean = false,
    val message: String = "",
    @SerializedName("needs_attention")
    val needsAttention: Boolean = false
)

data class VibeCodeState(
    val codex: VibeCodeToolState = VibeCodeToolState(),
    val claude: VibeCodeToolState = VibeCodeToolState(),
    val opencode: VibeCodeToolState = VibeCodeToolState()
)

data class SystemState(
    val cpu: CpuState = CpuState(),
    val memory: MemoryState = MemoryState(),
    val temperature: TemperatureState = TemperatureState(),
    val gpu: GpuState = GpuState(),
    val uptime: Long = 0  // System uptime in seconds
)

data class CpuState(
    val usage: Float = 0f,  // CPU usage percentage (0-100)
    @SerializedName("core_count")
    val coreCount: Int = 0,
    @SerializedName("model")
    val model: String = ""
)

data class MemoryState(
    val used: Long = 0,     // Used memory in bytes
    val total: Long = 0,    // Total memory in bytes
    val percentage: Float = 0f  // Memory usage percentage (0-100)
)

data class TemperatureState(
    val cpu: Float = 0f,    // CPU temperature in Celsius
    val gpu: Float = 0f     // GPU temperature in Celsius (if available)
)

data class GpuState(
    val model: String = "",  // GPU model name
    val usage: Float = 0f    // GPU usage percentage (0-100)
)

data class FullState(
    val media: MediaState = MediaState(),
    val volume: VolumeState = VolumeState(),
    val vibecode: VibeCodeState = VibeCodeState(),
    val system: SystemState = SystemState()
)

data class ServerMessage(
    val type: String = "",
    val data: Any? = null,
    val error: String? = null
)

data class ClientCommand(
    val action: String,
    val command: String? = null,
    val level: Int? = null,
    val step: Int? = null
)
