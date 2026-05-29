import asyncio
import signal
import sys
from websocket_server import WebSocketServer
from media_monitor import MediaMonitor
from volume_control import VolumeControl
from vibecode_monitor import VibeCodeMonitor
from system_monitor import SystemMonitor

class DesktopAgent:
    """Main desktop agent that coordinates all monitors"""
    
    def __init__(self):
        self.ws_server = WebSocketServer()
        self.media_monitor = MediaMonitor()
        self.volume_control = VolumeControl()
        self.vibecode_monitor = VibeCodeMonitor()
        self.system_monitor = SystemMonitor()
        self.running = False
        self.last_state = None
        self.broadcast_interval = 2.0  # seconds - reduced frequency for battery optimization
        
        # Set up message handler
        self.ws_server.set_message_handler(self.handle_message)
    
    async def handle_message(self, data: dict) -> dict:
        """Handle incoming message from client"""
        action = data.get("action")
        
        if action == "get_state":
            return await self.get_full_state()
        
        elif action == "media_control":
            command = data.get("command")
            success = self.media_monitor.send_media_command(command)
            return {"success": success}
        
        elif action == "set_volume":
            level = data.get("level", 50)
            success = self.volume_control.set_volume(level)
            return {"success": success}
        
        elif action == "toggle_mute":
            success = self.volume_control.toggle_mute()
            return {"success": success}
        
        elif action == "volume_up":
            step = data.get("step", 10)
            success = self.volume_control.volume_up(step)
            return {"success": success}
        
        elif action == "volume_down":
            step = data.get("step", 10)
            success = self.volume_control.volume_down(step)
            return {"success": success}
        
        return {"error": "Unknown action"}
    
    async def get_full_state(self) -> dict:
        """Get complete system state"""
        return {
            "type": "full_state",
            "data": {
                "media": self.media_monitor.get_media_state(),
                "volume": self.volume_control.get_volume_state(),
                "vibecode": self.vibecode_monitor.get_all_tools_status(),
                "system": self.system_monitor.get_system_state()
            }
        }
    
    def state_changed(self, new_state: dict) -> bool:
        """Check if state has changed significantly"""
        if self.last_state is None:
            return True
        
        # Compare only essential fields to reduce unnecessary updates
        last_data = self.last_state.get("data", {})
        new_data = new_state.get("data", {})
        
        # Check media state changes
        last_media = last_data.get("media", {})
        new_media = new_data.get("media", {})
        if (last_media.get("playing") != new_media.get("playing") or
            last_media.get("title") != new_media.get("title") or
            last_media.get("artist") != new_media.get("artist")):
            return True
        
        # Check volume changes
        last_volume = last_data.get("volume", {})
        new_volume = new_data.get("volume", {})
        if (last_volume.get("level") != new_volume.get("level") or
            last_volume.get("muted") != new_volume.get("muted")):
            return True
        
        # Check vibecode changes
        last_vibecode = last_data.get("vibecode", {})
        new_vibecode = new_data.get("vibecode", {})
        for tool in ["codex", "claude", "opencode"]:
            last_tool = last_vibecode.get(tool, {})
            new_tool = new_vibecode.get(tool, {})
            if (last_tool.get("status") != new_tool.get("status") or
                last_tool.get("running") != new_tool.get("running") or
                last_tool.get("needs_attention") != new_tool.get("needs_attention")):
                return True
        
        # Check system state changes (with threshold to avoid too frequent updates)
        last_system = last_data.get("system", {})
        new_system = new_data.get("system", {})
        last_cpu = last_system.get("cpu", {})
        new_cpu = new_system.get("cpu", {})
        last_mem = last_system.get("memory", {})
        new_mem = new_system.get("memory", {})
        last_gpu = last_system.get("gpu", {})
        new_gpu = new_system.get("gpu", {})
        
        # Use 5% threshold for CPU and memory changes
        if abs(last_cpu.get("usage", 0) - new_cpu.get("usage", 0)) > 5:
            return True
        if abs(last_mem.get("percentage", 0) - new_mem.get("percentage", 0)) > 5:
            return True
        
        # Check GPU usage changes
        if abs(last_gpu.get("usage", 0) - new_gpu.get("usage", 0)) > 5:
            return True
        
        # Check temperature changes (5 degree threshold)
        last_temp = last_system.get("temperature", {})
        new_temp = new_system.get("temperature", {})
        if abs(last_temp.get("cpu", 0) - new_temp.get("cpu", 0)) > 5:
            return True
        if abs(last_temp.get("gpu", 0) - new_temp.get("gpu", 0)) > 5:
            return True
        
        return False
    
    async def state_broadcaster(self):
        """Periodically broadcast state to all clients only when changed"""
        while self.running:
            try:
                state = await self.get_full_state()
                
                # Only broadcast if state has changed
                if self.state_changed(state):
                    await self.ws_server.broadcast(state)
                    self.last_state = state
                
                await asyncio.sleep(self.broadcast_interval)
            except Exception as e:
                print(f"Broadcast error: {e}")
                await asyncio.sleep(1)
    
    async def run(self):
        """Run the desktop agent"""
        self.running = True
        print("Desktop Agent starting...")
        print("Press Ctrl+C to stop")
        
        # Start WebSocket server and state broadcaster
        await asyncio.gather(
            self.ws_server.start(),
            self.state_broadcaster()
        )
    
    def stop(self):
        """Stop the desktop agent"""
        self.running = False
        print("\nDesktop Agent stopping...")

def main():
    agent = DesktopAgent()
    
    # Handle graceful shutdown
    def signal_handler(sig, frame):
        agent.stop()
        sys.exit(0)
    
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    try:
        asyncio.run(agent.run())
    except KeyboardInterrupt:
        agent.stop()

if __name__ == "__main__":
    main()
