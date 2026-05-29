#!/usr/bin/env python3
"""Test script for desktop agent components"""

from media_monitor import MediaMonitor
from volume_control import VolumeControl
from vibecode_monitor import VibeCodeMonitor

def test_media_monitor():
    print("=== Testing Media Monitor ===")
    monitor = MediaMonitor()
    state = monitor.get_media_state()
    print(f"Media state: {state}")
    print()

def test_volume_control():
    print("=== Testing Volume Control ===")
    control = VolumeControl()
    state = control.get_volume_state()
    print(f"Volume state: {state}")
    print()

def test_vibecode_monitor():
    print("=== Testing VibeCode Monitor ===")
    monitor = VibeCodeMonitor()
    state = monitor.get_all_tools_status()
    print(f"VibeCode state: {state}")
    print()

if __name__ == "__main__":
    print("Testing Desktop Agent Components\n")
    
    test_media_monitor()
    test_volume_control()
    test_vibecode_monitor()
    
    print("Tests completed!")
