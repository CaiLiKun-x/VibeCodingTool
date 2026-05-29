# Desktop Agent

macOS desktop agent for VibeCoding Tool remote control.

## Installation

```bash
pip install -r requirements.txt
```

## Usage

```bash
python main.py
```

The agent will start a WebSocket server on `ws://0.0.0.0:8765`.

## Features

- **Media Control**: Monitor and control Apple Music, Spotify
- **Volume Control**: System volume control with mute toggle
- **VibeCoding Monitor**: Track Codex, Claude, OpenCode status

## WebSocket Protocol

### Commands (from client)

```json
{"action": "get_state"}
{"action": "media_control", "command": "play|pause|next|previous"}
{"action": "set_volume", "level": 50}
{"action": "toggle_mute"}
{"action": "volume_up", "step": 10}
{"action": "volume_down", "step": 10}
```

### State Updates (to client)

```json
{
  "type": "full_state",
  "data": {
    "media": {
      "playing": true,
      "title": "Song Name",
      "artist": "Artist",
      "position": 120,
      "duration": 240,
      "app": "Apple Music"
    },
    "volume": {
      "level": 75,
      "muted": false
    },
    "vibecode": {
      "codex": {"status": "inactive", "running": false},
      "claude": {"status": "running", "running": true},
      "opencode": {"status": "waiting_input", "running": true, "needs_attention": true}
    }
  }
}
```
