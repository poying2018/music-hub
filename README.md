<h1 align="center">Music Hub</h1>

<p align="center">
  <strong>一个纯粹、精致的 Android 液态玻璃高颜值本地音乐播放器</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language">
  <img src="https://img.shields.io/badge/UI-Compose%20Multiplatform-purple.svg" alt="UI">
  <img src="https://img.shields.io/badge/Version-v1.0.0-teal.svg" alt="Version">
  <img src="https://img.shields.io/badge/License-MIT-orange.svg" alt="License">
</p>

---

## 🌟 项目亮点

Music Hub 专注于打造纯粹、无干扰的本地音乐体验，采用**现代液态玻璃（Liquid Glass）拟真材质与高质感动效**，摆脱流媒体杂乱推荐与广告，让听歌回归纯粹本质。

- 💧 **液态玻璃美学**：基于实时层级采样的磨砂玻璃容器、液态弹性开关、高对比度播放控制与流体底部导航栏。
- 🎵 **应用私有隔离曲库**：音乐存储于应用私有目录，完全独立于系统的 MediaStore，杜绝手机系统相册与录音杂音污染。
- 🌐 **WiFi 局域网一键传歌**：同一局域网内，电脑/平板浏览器打开手机显示的地址即可拖拽上传歌曲与同名歌词；内置上传队列机制，稳定高效。
- ☁️ **WebDAV 网盘直连**：支持挂载坚果云、Alist、Nextcloud、群晖 NAS 等私有云存储，云端曲库自由浏览与高速下载。
- 📁 **多文件与分享导入**：支持从手机本地存储多选导入音频，同名 `.lrc` 歌词文件自动关联。
- 🎤 **超微歌词与平滑跟随**：毫秒级 LRC 逐行解析、高帧率平滑滚动与动态高亮跟随。
- ⚡ **现代系统媒体集成**：Android 前台播放服务、系统锁屏控制卡片、通知栏媒体控制器，支持音频焦点管理与拔出耳机自动暂停。

---

## 🎧 支持音频与歌词格式

- **音频**：MP3, FLAC, WAV, M4A, OGG, AAC, OPUS
- **歌词**：LRC 格式（同名歌词文件自动与音频绑定，支持更新覆盖）

---

## 🛠️ 技术栈

- **语言**：Kotlin 2.4+
- **界面框架**：Compose Multiplatform 1.10+ / Jetpack Compose
- **设计系统**：Material Design 3 + Custom Liquid Glass Backdrop Shader Engine
- **多媒体**：Android MediaPlayer / MediaSessionCompat
- **网络传输**：轻量级私有 ServerSocket（带 WiFiLock / WakeLock 保护机制，支持 CORS 与模拟器端口映射）
- **图片加载**：Coil 3
- **项目工程**：Gradle 8.11+ / Kotlin Multiplatform

---

## 🚀 快速开始

### 环境要求
- JDK 17 或更高版本
- Android SDK 35 (Android 15)

### 编译 Debug 版本
```bash
# Linux / macOS
./gradlew :androidApp:assembleDebug

# Windows PowerShell
.\gradlew.bat :androidApp:assembleDebug
```
产物位置：`androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### 编译正式 Release 版本
在项目根目录配置 `keystore.properties`（参考签名密钥配置）：
```properties
storeFile=lazer-release.jks
storePassword=your_password
keyAlias=lazer
keyPassword=your_password
```
执行编译：
```bash
.\gradlew.bat :androidApp:assembleRelease
```
产物位置：`androidApp/build/outputs/apk/release/androidApp-release.apk`

---

## 📲 WiFi 传歌使用说明

1. 手机连接家庭或办公室 WiFi；
2. 打开 Music Hub，点击右上角 **「导入音乐」 -> 「WiFi 导入」**；
3. 点击 **「开启传输服务」**；
4. 电脑浏览器直接访问手机屏幕上显示的地址（例如 `http://192.168.1.100:8765`）；
5. 拖拽音频文件（如 `.mp3`, `.flac`）及同名歌词文件（`.lrc`）到网页中即可批量完成导入。

> 💡 **模拟器测试提示**：
> 若在 Android 模拟器（如 MuMu、AVD）中运行测试，请在宿主机终端执行端口映射：
> ```bash
> adb forward tcp:8765 tcp:8765
> ```
> 随后在电脑浏览器打开 `http://127.0.0.1:8765` 即可正常传歌。

---

## 📄 许可证

本项目基于 [MIT License](./LICENSE) 开放源代码。
