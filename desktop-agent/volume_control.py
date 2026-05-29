import subprocess
from typing import Dict, Any

class VolumeControl:
    """macOS volume control using osascript"""
    
    def get_volume_state(self) -> Dict[str, Any]:
        """Get current volume state"""
        try:
            output = self._run_osascript('get volume settings')
            # Parse output: "output volume:50, input volume:75, alert volume:100, output muted:false"
            parts = output.split(",")
            volume = 0
            muted = False
            
            for part in parts:
                part = part.strip()
                if "output volume" in part:
                    volume = int(part.split(":")[1])
                elif "output muted" in part:
                    muted = part.split(":")[1].strip().lower() == "true"
            
            return {
                "level": volume,
                "muted": muted
            }
        except Exception as e:
            return {"error": str(e)}
    
    def set_volume(self, level: int) -> bool:
        """Set volume level (0-100)"""
        try:
            level = max(0, min(100, level))
            self._run_osascript(f'set volume output volume {level}')
            return True
        except:
            return False
    
    def toggle_mute(self) -> bool:
        """Toggle mute state"""
        try:
            current = self.get_volume_state()
            if "error" in current:
                return False
            new_muted = not current["muted"]
            self._run_osascript(f'set volume output muted {str(new_muted).lower()}')
            return True
        except:
            return False
    
    def volume_up(self, step: int = 10) -> bool:
        """Increase volume by step"""
        try:
            current = self.get_volume_state()
            if "error" in current:
                return False
            new_level = min(100, current["level"] + step)
            return self.set_volume(new_level)
        except:
            return False
    
    def volume_down(self, step: int = 10) -> bool:
        """Decrease volume by step"""
        try:
            current = self.get_volume_state()
            if "error" in current:
                return False
            new_level = max(0, current["level"] - step)
            return self.set_volume(new_level)
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
