# VibeCoding Tool 开发计划

## 项目概述

开发一款安卓应用，用于远程监控和控制电脑，主要功能包括：
- 连接电脑端（WebSocket 无线连接）
- 同步电脑端播放状态（音乐/视频）
- 同步 VibeCoding 工具状态（Codex、Claude、OpenCode 等）
- 调节电脑端音量
- 横屏全屏展示

---

## 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Android App (横屏)                      │
│  ┌─────────────┬──────────────┬─────────────┬─────────────┐ │
│  │  播放状态    │  VibeCoding  │   音量控制   │  连接状态   │ │
│  └─────────────┴──────────────┴─────────────┴─────────────┘ │
└──────────────────────────┬──────────────────────────────────┘
                           │ WebSocket (ws://IP:8765)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    电脑端 Agent (Python)                      │
│  ┌─────────────┬──────────────┬─────────────┬─────────────┐ │
│  │  媒体检测    │  VibeCoding  │   系统音量   │  WebSocket  │ │
│  │  (osascript) │  进程监控    │   控制       │  Server     │ │
│  └─────────────┴──────────────┴─────────────┴─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

| 组件 | 技术 | 说明 |
|------|------|------|
| Android | Kotlin + Jetpack Compose | 声明式 UI，横屏适配 |
| 通信 | WebSocket | 实时双向通信 |
| 电脑端 | Python + asyncio | 跨平台，macOS 支持 |
| 协议 | JSON | 简单易调试 |

---

## 开发阶段

### Phase 1: 电脑端基础服务 ✅

**目标：** 实现 macOS 电脑端 Agent，支持媒体状态监控和 WebSocket 服务

**任务：**
- [x] 创建项目目录结构
- [x] 实现 WebSocket 服务端 (`websocket_server.py`)
- [x] 实现 macOS 媒体状态检测 (`media_monitor.py`)
  - 支持 Apple Music
  - 支持 Spotify
  - 支持浏览器媒体检测
- [x] 实现系统音量控制 (`volume_control.py`)
- [x] 实现 VibeCoding 工具监控 (`vibecode_monitor.py`)
  - 支持 Codex
  - 支持 Claude
  - 支持 OpenCode
- [x] 主程序入口 (`main.py`)

**验收标准：**
- 运行 `python main.py` 启动 WebSocket 服务
- 能够获取当前播放状态
- 能够获取/设置系统音量

---

### Phase 2: Android 基础框架 ✅

**目标：** 搭建 Android 项目骨架，实现 WebSocket 连接

**任务：**
- [x] 创建 Android 项目结构
- [x] 配置 Gradle 依赖
- [x] 实现 WebSocket 客户端 (`WebSocketClient.kt`)
- [x] 实现数据模型 (`Models.kt`)
- [x] 实现 ViewModel 状态管理 (`MainViewModel.kt`)

**验收标准：**
- Android 应用可以连接到电脑端 WebSocket 服务
- 能够接收并解析状态数据

---

### Phase 3: UI 界面实现 ✅

**目标：** 实现横屏全屏 UI，包含所有功能面板

**任务：**
- [x] 实现主题系统 (`Theme.kt`, `Color.kt`, `Type.kt`)
- [x] 实现媒体面板 (`MediaPanel.kt`)
  - 显示歌曲信息
  - 播放控制按钮
  - 进度条
- [x] 实现音量面板 (`VolumePanel.kt`)
  - 音量滑块
  - 静音按钮
  - 音量增减按钮
- [x] 实现 VibeCoding 面板 (`VibeCodePanel.kt`)
  - 工具状态显示
  - 需要干预提示
- [x] 实现连接面板 (`ConnectionPanel.kt`)
  - 服务器地址输入
  - 连接/断开按钮
- [x] 实现主界面布局 (`MainScreen.kt`)
- [x] 配置横屏全屏 (`AndroidManifest.xml`)

**验收标准：**
- 应用启动后横屏全屏显示
- 四个面板正确布局
- UI 响应状态变化

---

### Phase 4: 功能联调 ✅

**目标：** 完善前后端通信，实现完整功能

**任务：**
- [x] 实现媒体控制命令
  - 播放/暂停
  - 上一曲/下一曲
- [x] 实现音量控制命令
  - 设置音量
  - 切换静音
- [x] 实现状态实时更新
- [x] 错误处理和提示

**验收标准：**
- 可以控制电脑端媒体播放
- 可以调节电脑端音量
- 状态实时同步

---

### Phase 5: 优化和完善 ⏳

**目标：** 优化用户体验，完善细节

**任务：**
- [ ] 添加连接状态动画
- [ ] 优化横屏布局适配
- [ ] 添加设置页面
  - 自动连接选项
  - 主题切换
- [ ] 添加通知栏控制
- [ ] 优化电池消耗
- [ ] 添加 ADB 连接方式（备选）

**验收标准：**
- 动画流畅
- 各种屏幕尺寸适配良好
- 电池消耗合理

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
      "codex": {
        "status": "inactive",
        "running": false,
        "message": "",
        "needs_attention": false
      },
      "claude": {
        "status": "running",
        "running": true,
        "message": "Processing...",
        "needs_attention": false
      },
      "opencode": {
        "status": "waiting_input",
        "running": true,
        "message": "Apply changes to main.py? (y/n)",
        "needs_attention": true
      }
    }
  }
}
```

---

## 目录结构

```
VibeCodingTool/
├── desktop-agent/                  # 电脑端 Agent
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
│   │   ├── build.gradle.kts        # 应用级构建配置
│   │   └── src/main/
│   │       ├── AndroidManifest.xml # 应用清单
│   │       ├── java/com/vibecodingtool/
│   │       │   ├── MainActivity.kt # 主 Activity
│   │       │   ├── ui/             # UI 组件
│   │       │   │   ├── MainScreen.kt
│   │       │   │   ├── MediaPanel.kt
│   │       │   │   ├── VolumePanel.kt
│   │       │   │   ├── VibeCodePanel.kt
│   │       │   │   ├── ConnectionPanel.kt
│   │       │   │   └── theme/
│   │       │   │       ├── Color.kt
│   │       │   │       ├── Theme.kt
│   │       │   │       └── Type.kt
│   │       │   ├── data/           # 数据模型
│   │       │   │   └── Models.kt
│   │       │   ├── network/        # 网络层
│   │       │   │   └── WebSocketClient.kt
│   │       │   └── viewmodel/      # ViewModel
│   │       │       └── MainViewModel.kt
│   │       └── res/                # 资源文件
│   │           └── values/
│   │               ├── strings.xml
│   │               └── themes.xml
│   ├── build.gradle.kts            # 项目级构建配置
│   ├── settings.gradle.kts         # 项目设置
│   └── gradle.properties           # Gradle 属性
│
├── PLAN.md                         # 开发计划（本文件）
├── README.md                       # 项目说明
└── .gitignore                      # Git 忽略配置
```

---

## 环境要求

### 电脑端
- macOS 14.7.8
- Python 3.8+
- pip

### Android 端
- Android Studio Hedgehog+
- Android SDK 34
- Kotlin 1.9.20

---

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/CaiLiKun-x/VibeCodingTool.git
cd VibeCodingTool
```

### 2. 启动电脑端

```bash
cd desktop-agent
pip install -r requirements.txt
python main.py
```

### 3. 构建 Android 应用

用 Android Studio 打开 `android-app` 目录，构建并安装到手机。

### 4. 连接

1. 确保手机和电脑在同一网络
2. 在应用中输入电脑 IP：`ws://你的IP:8765`
3. 点击 "Connect"

---

## 待优化项

1. **媒体控制**：当前主要支持 Apple Music，可扩展更多播放器
2. **VibeCoding 监控**：需要根据实际工具日志格式调整正则
3. **ADB 连接**：作为 WebSocket 的备选方案
4. **安全性**：添加认证机制，防止未授权访问
5. **性能**：优化状态更新频率，减少电量消耗
