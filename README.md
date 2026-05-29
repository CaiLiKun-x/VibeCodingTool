# VibeCoding Tool

<p align="center">
  <img src="https://img.shields.io/badge/platform-macOS%20%7C%20Android-blue" alt="Platform">
  <img src="https://img.shields.io/badge/python-3.8+-green" alt="Python">
  <img src="https://img.shields.io/badge/kotlin-1.9.20-purple" alt="Kotlin">
  <img src="https://img.shields.io/badge/license-MIT-yellow" alt="License">
</p>

远程控制电脑的安卓应用，支持媒体播放控制、音量调节、VibeCoding 工具状态监控。

<p align="center">
  <img src="docs/screenshot.png" alt="Screenshot" width="600">
</p>

---

## 功能特性

### 🎵 媒体控制
- 显示当前播放的歌曲信息（标题、艺术家、专辑）
- 播放/暂停/上一曲/下一曲控制
- 实时进度条显示
- 支持 Apple Music、Spotify

### 🔊 音量控制
- 实时显示音量百分比
- 滑块调节音量
- 一键静音切换
- 音量增减按钮

### 🤖 VibeCoding 状态监控
- 监控 Codex、Claude、OpenCode 等工具
- 显示运行状态（运行中/等待输入/空闲）
- 需要人工干预时高亮提示
- 支持查看详细信息

### 📱 连接管理
- WebSocket 无线连接
- 显示连接状态
- 支持修改服务器地址
- 一键连接/断开

### 🖥️ 横屏全屏
- 专为横屏设计
- 全屏沉浸式体验
- 四面板布局，信息一目了然

---

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/CaiLiKun-x/VibeCodingTool.git
cd VibeCodingTool
```

### 2. 启动电脑端 Agent

```bash
cd desktop-agent
pip install -r requirements.txt
python main.py
```

Agent 将在 `ws://0.0.0.0:8765` 启动 WebSocket 服务。

### 3. 构建 Android 应用

1. 使用 Android Studio 打开 `android-app` 目录
2. 同步 Gradle 依赖
3. 构建并安装到手机

### 4. 连接

1. 确保手机和电脑在同一网络
2. 在 Android 应用中输入电脑的 IP 地址
   - 格式：`ws://你的IP:8765`
   - 例如：`ws://192.168.1.100:8765`
3. 点击 "Connect"

---

## 项目结构

```
VibeCodingTool/
├── desktop-agent/                  # macOS 电脑端 Agent (Python)
│   ├── main.py                     # 主程序入口
│   ├── media_monitor.py            # 媒体状态监控
│   ├── volume_control.py           # 音量控制
│   ├── vibecode_monitor.py         # VibeCoding 工具监控
│   ├── websocket_server.py         # WebSocket 服务端
│   ├── test_agent.py               # 测试脚本
│   ├── requirements.txt            # Python 依赖
│   └── README.md                   # 电脑端说明
│
├── android-app/                    # Android 客户端
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/vibecodingtool/
│   │       │   ├── MainActivity.kt
│   │       │   ├── ui/             # UI 组件
│   │       │   ├── data/           # 数据模型
│   │       │   ├── network/        # 网络层
│   │       │   └── viewmodel/      # 状态管理
│   │       └── res/                # 资源文件
│   └── build.gradle.kts
│
├── PLAN.md                         # 开发计划
├── README.md                       # 本文件
└── .gitignore
```

---

## 技术栈

| 组件 | 技术 | 说明 |
|------|------|------|
| **Android** | Kotlin + Jetpack Compose | 声明式 UI，横屏适配 |
| **通信** | WebSocket | 实时双向通信 |
| **电脑端** | Python + asyncio | 跨平台，macOS 支持 |
| **协议** | JSON | 简单易调试 |

---

## 通信协议

### 客户端 → 服务端

```json
// 获取完整状态
{"action": "get_state"}

// 媒体控制
{"action": "media_control", "command": "play|pause|next|previous"}

// 音量控制
{"action": "set_volume", "level": 50}
{"action": "toggle_mute"}
{"action": "volume_up", "step": 10}
{"action": "volume_down", "step": 10}
```

### 服务端 → 客户端

```json
{
  "type": "full_state",
  "data": {
    "media": {
      "playing": true,
      "title": "Song Name",
      "artist": "Artist",
      "album": "Album",
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

---

## 系统要求

### 电脑端
- macOS 14.7.8+
- Python 3.8+
- pip

### Android 端
- Android 8.0+ (API 26+)
- Android Studio Hedgehog+ (构建应用)

---

## 测试

### 测试电脑端组件

```bash
cd desktop-agent
python test_agent.py
```

### 测试 WebSocket 连接

```bash
# 安装 wscat
npm install -g wscat

# 连接到服务
wscat -c ws://localhost:8765

# 发送命令
{"action": "get_state"}
```

---

## 注意事项

1. **权限授权**：首次运行需要授权 AppleScript 控制权限（系统偏好设置 → 安全性与隐私 → 辅助功能）
2. **防火墙**：确保防火墙允许 8765 端口
3. **网络**：手机和电脑需在同一局域网
4. **播放器**：媒体控制主要支持 Apple Music 和 Spotify

---

## 开发计划

详细的开发计划请查看 [PLAN.md](PLAN.md)

---

## 贡献

欢迎提交 Issue 和 Pull Request！

---

## 许可证

MIT License

---

## 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [websockets](https://websockets.readthedocs.io/)
- [psutil](https://github.com/giampaolo/psutil)
