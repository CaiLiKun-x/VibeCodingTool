import asyncio
import signal
import sys
from websocket_server import WebSocketServer
from media_monitor import MediaMonitor
from volume_control import VolumeControl
from vibecode_monitor import VibeCodeMonitor

class DesktopAgent:
    """Main desktop agent that coordinates all monitors"""
    
    def __init__(self):
        self.ws_server = WebSocketServer()
        self.media_monitor = MediaMonitor()
        self.volume_control = VolumeControl()
        self.vibecode_monitor = VibeCodeMonitor()
        self.running = False
        
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
                "vibecode": self.vibecode_monitor.get_all_tools_status()
            }
        }
    
    async def state_broadcaster(self):
        """Periodically broadcast state to all clients"""
        while self.running:
            try:
                state = await self.get_full_state()
                await self.ws_server.broadcast(state)
                await asyncio.sleep(1)  # Update every second
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
