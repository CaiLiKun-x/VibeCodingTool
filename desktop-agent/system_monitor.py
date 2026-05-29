import platform
import subprocess
import os
from typing import Dict, Any, Optional

try:
    import psutil
    HAS_PSUTIL = True
except ImportError:
    HAS_PSUTIL = False
    print("Warning: psutil not installed. System monitoring disabled.")
    print("Install with: pip install psutil")

class SystemMonitor:
    """Monitor system resources (CPU, memory, temperature, GPU)"""
    
    def __init__(self):
        self.system = platform.system()
        self.cpu_count = self._get_cpu_count()
        self.cpu_model = self._get_cpu_model()
        self.gpu_model = self._get_gpu_model()
        self._temp_tool = self._find_temperature_tool()
    
    def _get_cpu_count(self) -> int:
        """Get CPU core count"""
        try:
            if HAS_PSUTIL:
                return psutil.cpu_count()
            import os
            return os.cpu_count() or 1
        except:
            return 1
    
    def _get_cpu_model(self) -> str:
        """Get CPU model name"""
        try:
            if self.system == "Darwin":
                result = subprocess.run(
                    ["sysctl", "-n", "machdep.cpu.brand_string"],
                    capture_output=True, text=True, timeout=5
                )
                return result.stdout.strip()
            elif self.system == "Linux":
                with open("/proc/cpuinfo", "r") as f:
                    for line in f:
                        if "model name" in line:
                            return line.split(":")[1].strip()
            return platform.processor() or "Unknown"
        except:
            return "Unknown"
    
    def _get_gpu_model(self) -> str:
        """Get GPU model name"""
        try:
            if self.system == "Darwin":
                # Try to get GPU info on macOS
                result = subprocess.run(
                    ["system_profiler", "SPDisplaysDataType"],
                    capture_output=True, text=True, timeout=5
                )
                for line in result.stdout.split("\n"):
                    if "Chipset Model:" in line or "Chip:" in line:
                        return line.split(":")[1].strip()
            elif self.system == "Linux":
                # Try lspci for GPU info
                result = subprocess.run(
                    ["lspci"],
                    capture_output=True, text=True, timeout=5
                )
                for line in result.stdout.split("\n"):
                    if "VGA" in line or "3D" in line:
                        # Extract GPU model from lspci output
                        parts = line.split(":")
                        if len(parts) > 1:
                            return parts[-1].strip()
            return "Unknown"
        except:
            return "Unknown"
    
    def _find_temperature_tool(self) -> Optional[str]:
        """Find available temperature monitoring tool"""
        tools = ["osx-cpu-temp", "sensors", "vcgencmd"]
        for tool in tools:
            try:
                result = subprocess.run(
                    ["which", tool],
                    capture_output=True,
                    text=True,
                    timeout=2
                )
                if result.returncode == 0:
                    return tool
            except:
                continue
        return None
    
    def get_system_state(self) -> Dict[str, Any]:
        """Get complete system state"""
        if not HAS_PSUTIL:
            return {
                "cpu": {"usage": 0.0, "core_count": self.cpu_count, "model": self.cpu_model},
                "memory": {"used": 0, "total": 0, "percentage": 0.0},
                "temperature": {"cpu": 0.0, "gpu": 0.0},
                "gpu": {"model": self.gpu_model, "usage": 0.0},
                "uptime": 0
            }
        
        return {
            "cpu": self._get_cpu_state(),
            "memory": self._get_memory_state(),
            "temperature": self._get_temperature_state(),
            "gpu": self._get_gpu_state(),
            "uptime": self._get_uptime()
        }
    
    def _get_cpu_state(self) -> Dict[str, Any]:
        """Get CPU state"""
        try:
            usage = psutil.cpu_percent(interval=0.1)
            return {
                "usage": usage,
                "core_count": self.cpu_count,
                "model": self.cpu_model
            }
        except:
            return {
                "usage": 0.0,
                "core_count": self.cpu_count,
                "model": self.cpu_model
            }
    
    def _get_memory_state(self) -> Dict[str, Any]:
        """Get memory state"""
        try:
            mem = psutil.virtual_memory()
            return {
                "used": mem.used,
                "total": mem.total,
                "percentage": mem.percent
            }
        except:
            return {
                "used": 0,
                "total": 0,
                "percentage": 0.0
            }
    
    def _get_temperature_state(self) -> Dict[str, Any]:
        """Get temperature state"""
        try:
            temps = psutil.sensors_temperatures()
            cpu_temp = 0.0
            gpu_temp = 0.0
            
            if temps:
                # Try common temperature sensor names
                for name in ["coretemp", "cpu_thermal", "k10temp", "zenpower", "cpu"]:
                    if name in temps:
                        cpu_temps = [t.current for t in temps[name] if t.current > 0]
                        if cpu_temps:
                            cpu_temp = sum(cpu_temps) / len(cpu_temps)
                            break
                
                # Try GPU temperature
                for name in ["gpu_thermal", "nvidia", "amdgpu", "gpu"]:
                    if name in temps:
                        gpu_temps = [t.current for t in temps[name] if t.current > 0]
                        if gpu_temps:
                            gpu_temp = gpu_temps[0]
                            break
            
            # macOS specific: use osx-cpu-temp or powermetrics
            if self.system == "Darwin" and cpu_temp == 0:
                cpu_temp = self._get_mac_temperature()
            
            return {
                "cpu": cpu_temp,
                "gpu": gpu_temp
            }
        except:
            return {
                "cpu": 0.0,
                "gpu": 0.0
            }
    
    def _get_gpu_state(self) -> Dict[str, Any]:
        """Get GPU state"""
        try:
            gpu_usage = 0.0
            
            # Try using nvidia-smi for NVIDIA GPUs
            try:
                result = subprocess.run(
                    ["nvidia-smi", "--query-gpu=utilization.gpu", "--format=csv,noheader,nounits"],
                    capture_output=True,
                    text=True,
                    timeout=5
                )
                if result.returncode == 0:
                    gpu_usage = float(result.stdout.strip())
            except:
                pass
            
            # Try using psutil for GPU (if available)
            if gpu_usage == 0 and HAS_PSUTIL:
                try:
                    # psutil doesn't have direct GPU monitoring
                    # but we can try to get it from other sources
                    pass
                except:
                    pass
            
            return {
                "model": self.gpu_model,
                "usage": gpu_usage
            }
        except:
            return {
                "model": self.gpu_model,
                "usage": 0.0
            }
    
    def _get_mac_temperature(self) -> float:
        """Get macOS CPU temperature"""
        try:
            # Try using osx-cpu-temp if available
            if self._temp_tool == "osx-cpu-temp":
                result = subprocess.run(
                    ["osx-cpu-temp"],
                    capture_output=True,
                    text=True,
                    timeout=5
                )
                if result.returncode == 0:
                    # Parse temperature (format: "XX.X°C" or "XX.X°F")
                    temp_str = result.stdout.strip().replace("°C", "").replace("°F", "")
                    try:
                        return float(temp_str)
                    except ValueError:
                        pass
            
            # Try using powermetrics (requires sudo, may not work)
            try:
                result = subprocess.run(
                    ["sudo", "powermetrics", "--samplers", "smc", "-n", "1", "-i", "100"],
                    capture_output=True, text=True,
                    timeout=5
                )
                for line in result.stdout.split("\n"):
                    if "CPU temperature" in line:
                        temp_str = line.split(":")[1].strip().replace("°C", "")
                        return float(temp_str)
            except:
                pass
            
            return 0.0
        except:
            return 0.0
    
    def _get_uptime(self) -> int:
        """Get system uptime in seconds"""
        try:
            return int(psutil.boot_time())
        except:
            return 0
