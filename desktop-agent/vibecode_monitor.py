import psutil
import os
import json
import re
import subprocess
from typing import Dict, Any, List, Optional
from pathlib import Path

class VibeCodeMonitor:
    """Monitor VibeCoding tools status"""
    
    def __init__(self):
        self.tools_config = {
            "codex": {
                "process_names": ["codex", "codex-cli", "codex.exe"],
                "log_paths": [
                    os.path.expanduser("~/.codex/log"),
                    os.path.expanduser("~/.config/codex/log"),
                    os.path.expanduser("~/Library/Logs/codex")
                ],
                "waiting_patterns": [
                    r"waiting for input",
                    r"approve.*\?",
                    r"y/n",
                    r"continue.*\?",
                    r"Do you want to",
                    r"Allow.*\?",
                    r"Permission.*\?"
                ]
            },
            "claude": {
                "process_names": ["claude", "claude-cli", "claude.exe", "anthropic"],
                "log_paths": [
                    os.path.expanduser("~/.claude/log"),
                    os.path.expanduser("~/.config/claude/log"),
                    os.path.expanduser("~/Library/Logs/Claude")
                ],
                "waiting_patterns": [
                    r"waiting for approval",
                    r"apply.*changes.*\?",
                    r"y/n",
                    r"continue.*\?",
                    r"Do you want to",
                    r"Allow.*\?",
                    r"Permission.*\?",
                    r"needs your approval"
                ]
            },
            "opencode": {
                "process_names": ["opencode", "opencode.exe", "opencode-cli"],
                "log_paths": [
                    os.path.expanduser("~/.opencode/log"),
                    os.path.expanduser("~/.config/opencode/log"),
                    os.path.expanduser("~/Library/Logs/opencode")
                ],
                "waiting_patterns": [
                    r"waiting for input",
                    r"approve.*\?",
                    r"y/n",
                    r"apply.*\?",
                    r"Do you want to",
                    r"Allow.*\?",
                    r"Permission.*\?"
                ]
            }
        }
        self._last_status = {}
    
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
                self._last_status[tool_name] = "inactive"
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
                self._last_status[tool_name] = "waiting_input"
                return {
                    "status": "waiting_input",
                    "running": True,
                    "message": waiting_state,
                    "needs_attention": True
                }
            
            # Check if we just transitioned from waiting to running
            last = self._last_status.get(tool_name, "inactive")
            self._last_status[tool_name] = "running"
            
            return {
                "status": "running",
                "running": True,
                "message": "Processing..." if last != "waiting_input" else "Continuing...",
                "needs_attention": False
            }
        except Exception as e:
            self._last_status[tool_name] = "error"
            return {
                "status": "error",
                "running": False,
                "message": str(e),
                "needs_attention": False
            }
    
    def _is_process_running(self, process_names: List[str]) -> bool:
        """Check if any of the process names are running"""
        try:
            for proc in psutil.process_iter(['name', 'cmdline', 'pid']):
                try:
                    proc_name = proc.info['name'].lower() if proc.info['name'] else ""
                    cmdline = " ".join(proc.info['cmdline']).lower() if proc.info['cmdline'] else ""
                    
                    for name in process_names:
                        name_lower = name.lower()
                        # Check process name
                        if name_lower in proc_name:
                            return True
                        # Check command line
                        if name_lower in cmdline:
                            return True
                        # Check if it's a node/python process running the tool
                        if ('node' in proc_name or 'python' in proc_name) and name_lower in cmdline:
                            return True
                except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
                    continue
            
            # Fallback: use 'pgrep' on Unix systems
            if os.name != 'nt':  # Not Windows
                for name in process_names:
                    try:
                        result = subprocess.run(
                            ['pgrep', '-f', name],
                            capture_output=True,
                            text=True,
                            timeout=2
                        )
                        if result.returncode == 0 and result.stdout.strip():
                            return True
                    except:
                        continue
        except Exception as e:
            print(f"Process check error: {e}")
        
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
        # Check last 50 lines (increased from 20)
        relevant_lines = lines[-50:] if len(lines) > 50 else lines
        
        # Look for waiting patterns in reverse order (most recent first)
        for line in reversed(relevant_lines):
            line_stripped = line.strip()
            if not line_stripped:
                continue
            for pattern in patterns:
                if re.search(pattern, line_stripped, re.IGNORECASE):
                    # Return a truncated version of the line
                    return line_stripped[:100] + "..." if len(line_stripped) > 100 else line_stripped
        return None
    
    def send_tool_command(self, tool_name: str, command: str) -> bool:
        """Send command to a tool (e.g., approve, reject)"""
        # This would need tool-specific implementation
        # For now, return False as we need more info about each tool's API
        return False
