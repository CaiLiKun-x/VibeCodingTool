import psutil
import os
import json
import re
from typing import Dict, Any, List, Optional
from pathlib import Path

class VibeCodeMonitor:
    """Monitor VibeCoding tools status"""
    
    def __init__(self):
        self.tools_config = {
            "codex": {
                "process_names": ["codex", "codex-cli"],
                "log_paths": [
                    os.path.expanduser("~/.codex/log"),
                    os.path.expanduser("~/.config/codex/log")
                ],
                "waiting_patterns": [
                    r"waiting for input",
                    r"approve.*\?",
                    r"y/n",
                    r"continue.*\?"
                ]
            },
            "claude": {
                "process_names": ["claude", "claude-cli"],
                "log_paths": [
                    os.path.expanduser("~/.claude/log"),
                    os.path.expanduser("~/.config/claude/log")
                ],
                "waiting_patterns": [
                    r"waiting for approval",
                    r"apply.*changes.*\?",
                    r"y/n",
                    r"continue.*\?"
                ]
            },
            "opencode": {
                "process_names": ["opencode"],
                "log_paths": [
                    os.path.expanduser("~/.opencode/log"),
                    os.path.expanduser("~/.config/opencode/log")
                ],
                "waiting_patterns": [
                    r"waiting for input",
                    r"approve.*\?",
                    r"y/n",
                    r"apply.*\?"
                ]
            }
        }
    
    def get_all_tools_status(self) -> Dict[str, Any]:
        """Get status of all monitored tools"""
        result = {}
        for tool_name, config in self.tools_config.items():
            result[tool_name] = self._get_tool_status(tool_name, config)
        return result
    
    def _get_tool_status(self, tool_name: str, config: Dict) -> Dict[str, Any]:
        """Get status of a specific tool"""
        try:
            # Check if process is running
            is_running = self._is_process_running(config["process_names"])
            
            if not is_running:
                return {
                    "status": "inactive",
                    "running": False,
                    "message": "",
                    "needs_attention": False
                }
            
            # Check logs for waiting state
            log_content = self._read_latest_log(config["log_paths"])
            waiting_state = self._check_waiting_state(log_content, config["waiting_patterns"])
            
            if waiting_state:
                return {
                    "status": "waiting_input",
                    "running": True,
                    "message": waiting_state,
                    "needs_attention": True
                }
            
            return {
                "status": "running",
                "running": True,
                "message": "Processing...",
                "needs_attention": False
            }
        except Exception as e:
            return {
                "status": "error",
                "running": False,
                "message": str(e),
                "needs_attention": False
            }
    
    def _is_process_running(self, process_names: List[str]) -> bool:
        """Check if any of the process names are running"""
        for proc in psutil.process_iter(['name', 'cmdline']):
            try:
                proc_name = proc.info['name'].lower() if proc.info['name'] else ""
                cmdline = " ".join(proc.info['cmdline']).lower() if proc.info['cmdline'] else ""
                
                for name in process_names:
                    if name.lower() in proc_name or name.lower() in cmdline:
                        return True
            except (psutil.NoSuchProcess, psutil.AccessDenied):
                continue
        return False
    
    def _read_latest_log(self, log_paths: List[str]) -> str:
        """Read the latest log file content"""
        for path in log_paths:
            if os.path.exists(path):
                try:
                    # If it's a directory, find the latest log file
                    if os.path.isdir(path):
                        log_files = sorted(
                            Path(path).glob("*.log"),
                            key=os.path.getmtime,
                            reverse=True
                        )
                        if log_files:
                            with open(log_files[0], 'r') as f:
                                # Read last 1000 chars
                                f.seek(0, 2)
                                size = f.tell()
                                f.seek(max(0, size - 1000))
                                return f.read()
                    else:
                        with open(path, 'r') as f:
                            f.seek(0, 2)
                            size = f.tell()
                            f.seek(max(0, size - 1000))
                            return f.read()
                except:
                    continue
        return ""
    
    def _check_waiting_state(self, log_content: str, patterns: List[str]) -> Optional[str]:
        """Check if the log indicates waiting for input"""
        if not log_content:
            return None
        
        lines = log_content.split('\n')
        # Check last 20 lines
        for line in reversed(lines[-20:]):
            for pattern in patterns:
                if re.search(pattern, line, re.IGNORECASE):
                    return line.strip()
        return None
    
    def send_tool_command(self, tool_name: str, command: str) -> bool:
        """Send command to a tool (e.g., approve, reject)"""
        # This would need tool-specific implementation
        # For now, return False as we need more info about each tool's API
        return False
