package com.vibecodingtool.data

import com.google.gson.annotations.SerializedName

data class MediaState(
    val playing: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val position: Double = 0.0,
    val duration: Double = 0.0,
    val app: String = "none"
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

data class FullState(
    val media: MediaState = MediaState(),
    val volume: VolumeState = VolumeState(),
    val vibecode: VibeCodeState = VibeCodeState()
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
