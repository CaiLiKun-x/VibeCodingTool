import subprocess
import json
from typing import Optional, Dict, Any

class MediaMonitor:
    """macOS media state monitor using osascript"""
    
    def get_media_state(self) -> Dict[str, Any]:
        """Get current media player state"""
        try:
            # Get current playing app
            app_name = self._run_osascript(
                'tell application "System Events" to get name of first process whose frontmost is true'
            )
            
            # Try Apple Music first
            state = self._get_apple_music_state()
            if state:
                return state
            
            # Try Spotify
            state = self._get_spotify_state()
            if state:
                return state
            
            # Try Chrome/Safari (YouTube etc.)
            state = self._get_browser_media_state()
            if state:
                return state
            
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
    
    def _get_apple_music_state(self) -> Optional[Dict[str, Any]]:
        """Get Apple Music state"""
        try:
            state = self._run_osascript(
                'tell application "Music" to get player state'
            )
            if "not running" in state.lower():
                return None
            
            playing = state == "playing"
            if not playing and state != "paused":
                return None
            
            title = self._run_osascript('tell application "Music" to get name of current track')
            artist = self._run_osascript('tell application "Music" to get artist of current track')
            album = self._run_osascript('tell application "Music" to get album of current track')
            position = self._run_osascript('tell application "Music" to get player position')
            duration = self._run_osascript('tell application "Music" to get duration of current track')
            
            return {
                "playing": playing,
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
            state = self._run_osascript(
                'tell application "Spotify" to get player state'
            )
            if "not running" in state.lower():
                return None
            
            playing = state == "playing"
            if not playing and state != "paused":
                return None
            
            title = self._run_osascript('tell application "Spotify" to get name of current track')
            artist = self._run_osascript('tell application "Spotify" to get artist of current track')
            album = self._run_osascript('tell application "Spotify" to get album of current track')
            position = self._run_osascript('tell application "Spotify" to get player position')
            duration = self._run_osascript('tell application "Spotify" to get duration of current track')
            
            return {
                "playing": playing,
                "title": title,
                "artist": artist,
                "album": album,
                "position": float(position) / 1000 if position else 0,
                "duration": float(duration) / 1000 if duration else 0,
                "app": "Spotify"
            }
        except:
            return None
    
    def _get_browser_media_state(self) -> Optional[Dict[str, Any]]:
        """Get browser media state (Chrome/Safari)"""
        try:
            # Check Chrome
            chrome_state = self._run_osascript('''
                tell application "Google Chrome"
                    if not running then return "not running"
                    set tabTitle to title of active tab of first window
                    return tabTitle
                end tell
            ''')
            
            if chrome_state and "not running" not in chrome_state.lower():
                return {
                    "playing": True,
                    "title": chrome_state,
                    "artist": "Chrome",
                    "album": "",
                    "position": 0,
                    "duration": 0,
                    "app": "Google Chrome"
                }
            
            return None
        except:
            return None
    
    def send_media_command(self, command: str) -> bool:
        """Send command to active media player"""
        try:
            if command == "play":
                self._run_osascript('tell application "Music" to play')
            elif command == "pause":
                self._run_osascript('tell application "Music" to pause')
            elif command == "next":
                self._run_osascript('tell application "Music" to next track')
            elif command == "previous":
                self._run_osascript('tell application "Music" to previous track')
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
