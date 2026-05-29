import subprocess
import json
import os
import re
from typing import Optional, Dict, Any, List

class MediaMonitor:
    """macOS media state monitor using osascript and MediaRemote"""
    
    def __init__(self):
        self._last_player = None  # Cache last active player
        # Chinese music apps that may not have AppleScript support
        self._chinese_music_apps = {
            "汽水音乐": ["qishui", "汽水音乐", "bytedance.music"],
            "QQ音乐": ["QQMusic", "qq音乐", "com.tencent.QQMusic"],
            "网易云音乐": ["NeteaseMusic", "网易云音乐", "com.netease.cloudmusic"],
            "酷狗音乐": ["KuGou", "酷狗音乐", "com.kugou.music"],
            "酷我音乐": ["KuwoMusic", "酷我音乐", "com.kuwo.music"],
            "喜马拉雅": ["Ximalaya", "喜马拉雅", "com.ximalaya.ting"],
            "Apple Music": ["Music", "com.apple.Music"],
            "Spotify": ["Spotify", "com.spotify.client"]
        }
    
    def get_media_state(self) -> Dict[str, Any]:
        """Get current media player state"""
        try:
            # Try last active player first for efficiency
            if self._last_player == "Music":
                state = self._get_apple_music_state()
                if state and state.get("playing"):
                    return state
            elif self._last_player == "Spotify":
                state = self._get_spotify_state()
                if state and state.get("playing"):
                    return state
            
            # Check all players with single combined script
            state = self._get_media_state_combined()
            if state:
                return state
            
            # Try to detect Chinese music apps
            state = self._get_chinese_music_state()
            if state:
                return state
            
            # Try using nowplaying-cli if available
            state = self._get_nowplaying_state()
            if state:
                return state
            
            self._last_player = None
            return {
                "playing": False,
                "title": "",
                "artist": "",
                "album": "",
                "position": 0,
                "duration": 0,
                "app": "none"
            }
        except Exception as e:
            return {"error": str(e)}
    
    def _get_media_state_combined(self) -> Optional[Dict[str, Any]]:
        """Get media state with single osascript call"""
        script = '''
        tell application "System Events"
            set musicRunning to (name of processes) contains "Music"
            set spotifyRunning to (name of processes) contains "Spotify"
        end tell
        
        set result to ""
        
        if musicRunning then
            try
                tell application "Music"
                    if player state is playing or player state is paused then
                        set result to "Music|" & (player state as text) & "|" & (name of current track) & "|" & (artist of current track) & "|" & (album of current track) & "|" & (player position) & "|" & (duration of current track)
                    end if
                end tell
            end try
        end if
        
        if result is "" and spotifyRunning then
            try
                tell application "Spotify"
                    if player state is playing or player state is paused then
                        set result to "Spotify|" & (player state as text) & "|" & (name of current track) & "|" & (artist of current track) & "|" & (album of current track) & "|" & (player position) & "|" & (duration of current track)
                    end if
                end tell
            end try
        end if
        
        return result
        '''
        
        output = self._run_osascript(script)
        if not output:
            return None
        
        parts = output.split("|")
        if len(parts) < 7:
            return None
        
        player, state, title, artist, album, position, duration = parts[:7]
        self._last_player = player
        
        return {
            "playing": state == "playing",
            "title": title,
            "artist": artist,
            "album": album,
            "position": float(position) if position else 0,
            "duration": float(duration) / 1000 if player == "Spotify" and duration else float(duration) if duration else 0,
            "app": player
        }
    
    def _get_apple_music_state(self) -> Optional[Dict[str, Any]]:
        """Get Apple Music state"""
        try:
            script = '''
            tell application "Music"
                if not running then return "not running"
                if player state is not playing and player state is not paused then return "stopped"
                return (player state as text) & "|" & (name of current track) & "|" & (artist of current track) & "|" & (album of current track) & "|" & (player position) & "|" & (duration of current track)
            end tell
            '''
            output = self._run_osascript(script)
            if "not running" in output.lower() or "stopped" in output.lower():
                return None
            
            parts = output.split("|")
            if len(parts) < 6:
                return None
            
            state, title, artist, album, position, duration = parts[:6]
            
            return {
                "playing": state == "playing",
                "title": title,
                "artist": artist,
                "album": album,
                "position": float(position) if position else 0,
                "duration": float(duration) if duration else 0,
                "app": "Apple Music"
            }
        except:
            return None
    
    def _get_spotify_state(self) -> Optional[Dict[str, Any]]:
        """Get Spotify state"""
        try:
            script = '''
            tell application "Spotify"
                if not running then return "not running"
                if player state is not playing and player state is not paused then return "stopped"
                return (player state as text) & "|" & (name of current track) & "|" & (artist of current track) & "|" & (album of current track) & "|" & (player position) & "|" & (duration of current track)
            end tell
            '''
            output = self._run_osascript(script)
            if "not running" in output.lower() or "stopped" in output.lower():
                return None
            
            parts = output.split("|")
            if len(parts) < 6:
                return None
            
            state, title, artist, album, position, duration = parts[:6]
            
            return {
                "playing": state == "playing",
                "title": title,
                "artist": artist,
                "album": album,
                "position": float(position) / 1000 if position else 0,
                "duration": float(duration) / 1000 if duration else 0,
                "app": "Spotify"
            }
        except:
            return None
    
    def _get_chinese_music_state(self) -> Optional[Dict[str, Any]]:
        """Detect Chinese music apps that may not have AppleScript support"""
        try:
            # Use psutil to check for running music apps
            import psutil
            
            detected_app = None
            for app_name, process_names in self._chinese_music_apps.items():
                for proc in psutil.process_iter(['name', 'cmdline']):
                    try:
                        proc_name = proc.info['name'].lower() if proc.info['name'] else ""
                        cmdline = " ".join(proc.info['cmdline']).lower() if proc.info['cmdline'] else ""
                        
                        for name in process_names:
                            if name.lower() in proc_name or name.lower() in cmdline:
                                detected_app = app_name
                                break
                        
                        if detected_app:
                            break
                    except:
                        continue
                
                if detected_app:
                    break
            
            if not detected_app:
                return None
            
            # Try to get window title using osascript (Accessibility API)
            try:
                script = f'''
                tell application "System Events"
                    set appName to first process whose name contains "{detected_app}"
                    set windowTitle to name of first window of appName
                    return windowTitle
                end tell
                '''
                window_title = self._run_osascript(script)
                
                if window_title and window_title.strip():
                    # Parse window title (usually "Song - Artist" or similar)
                    parts = window_title.split(" - ")
                    title = parts[0].strip() if len(parts) > 0 else ""
                    artist = parts[1].strip() if len(parts) > 1 else ""
                    
                    return {
                        "playing": True,  # Assume playing if window exists
                        "title": title,
                        "artist": artist,
                        "album": "",
                        "position": 0,
                        "duration": 0,
                        "app": detected_app
                    }
            except:
                pass
            
            # If we can't get window title, return basic info
            return {
                "playing": True,
                "title": "",
                "artist": "",
                "album": "",
                "position": 0,
                "duration": 0,
                "app": detected_app
            }
        except Exception as e:
            print(f"Chinese music detection error: {e}")
            return None
    
    def _get_nowplaying_state(self) -> Optional[Dict[str, Any]]:
        """Get media state using nowplaying-cli (if installed)"""
        try:
            # Check if nowplaying-cli is available
            result = subprocess.run(
                ["which", "nowplaying-cli"],
                capture_output=True,
                text=True,
                timeout=2
            )
            if result.returncode != 0:
                return None
            
            # Get current playing info
            result = subprocess.run(
                ["nowplaying-cli", "get-raw"],
                capture_output=True,
                text=True,
                timeout=5
            )
            
            if result.returncode == 0 and result.stdout.strip():
                data = json.loads(result.stdout)
                
                # Parse nowplaying-cli output
                title = data.get("title", "")
                artist = data.get("artist", "")
                album = data.get("album", "")
                duration = data.get("duration", 0)
                position = data.get("position", 0)
                is_playing = data.get("isPlaying", False)
                app = data.get("bundleIdentifier", "unknown")
                
                # Map bundle identifier to app name
                app_name = "Unknown"
                for name, identifiers in self._chinese_music_apps.items():
                    for identifier in identifiers:
                        if identifier.lower() in app.lower():
                            app_name = name
                            break
                
                return {
                    "playing": is_playing,
                    "title": title,
                    "artist": artist,
                    "album": album,
                    "position": float(position) if position else 0,
                    "duration": float(duration) if duration else 0,
                    "app": app_name
                }
        except (subprocess.TimeoutExpired, FileNotFoundError, json.JSONDecodeError):
            pass
        except Exception as e:
            print(f"nowplaying-cli error: {e}")
        
        return None
    
    def send_media_command(self, command: str) -> bool:
        """Send command to active media player"""
        try:
            player = self._last_player or "Music"
            
            # Try using nowplaying-cli first if available
            try:
                result = subprocess.run(
                    ["which", "nowplaying-cli"],
                    capture_output=True,
                    text=True,
                    timeout=2
                )
                if result.returncode == 0:
                    if command == "play":
                        subprocess.run(["nowplaying-cli", "play"], timeout=2)
                    elif command == "pause":
                        subprocess.run(["nowplaying-cli", "pause"], timeout=2)
                    elif command == "next":
                        subprocess.run(["nowplaying-cli", "next"], timeout=2)
                    elif command == "previous":
                        subprocess.run(["nowplaying-cli", "previous"], timeout=2)
                    return True
            except:
                pass
            
            # Fallback to AppleScript
            if command == "play":
                self._run_osascript(f'tell application "{player}" to play')
            elif command == "pause":
                self._run_osascript(f'tell application "{player}" to pause')
            elif command == "next":
                self._run_osascript(f'tell application "{player}" to next track')
            elif command == "previous":
                self._run_osascript(f'tell application "{player}" to previous track')
            return True
        except:
            return False
    
    def _run_osascript(self, script: str) -> str:
        """Run AppleScript command"""
        result = subprocess.run(
            ["osascript", "-e", script],
            capture_output=True,
            text=True,
            timeout=5
        )
        return result.stdout.strip()
