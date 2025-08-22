# 🔧 Termux CLI Manager

[![Build Status](https://github.com/anthropics/termux-cli-manager/workflows/Build%20and%20Release%20APK/badge.svg)](https://github.com/anthropics/termux-cli-manager/actions)
[![Release](https://img.shields.io/github/v/release/anthropics/termux-cli-manager)](https://github.com/anthropics/termux-cli-manager/releases)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

An Android native service that integrates with Termux to provide a unified interface for managing CLI tools like Claude CLI, Cursor CLI, and HuggingFace CLI through the Android Settings.

## 🚀 Features

### Core Functionality
- **🔧 CLI Tool Management**: Install, activate, and monitor multiple CLI tools
- **💬 Live Chat Interface**: Real-time interaction with CLI tools
- **📁 File System Browser**: Complete Termux file system access
- **🔄 Session Management**: Multiple concurrent CLI sessions
- **⚡ Command Execution**: Direct Termux command execution with live output
- **⚙️ Settings Integration**: Native Android Settings integration

### Supported CLI Tools
- **🤖 Claude CLI**: AI assistant integration
- **📝 Cursor CLI**: Code editor integration  
- **🤗 HuggingFace CLI**: ML model management

## 📱 Installation

### Option 1: Download Pre-built APK
1. Go to [Releases](https://github.com/anthropics/termux-cli-manager/releases)
2. Download the latest `termux-cli-manager-v*.apk`
3. Install on your Android device
4. Grant required permissions

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/anthropics/termux-cli-manager.git
cd termux-cli-manager

# Build the APK
./gradlew assembleDebug

# Install the APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🛠️ Prerequisites

### Android Device Requirements
- Android 8.0 (API level 26) or higher
- ARM64 processor (aarch64)
- At least 2GB of RAM
- 500MB+ free storage

### Termux Setup
```bash
# Install Termux from F-Droid (recommended)
# Update packages
pkg update && pkg upgrade -y

# Install required tools
pkg install nodejs python git -y
```

## 📋 Quick Start Guide

### 1. Initial Setup
1. **Install Termux CLI Manager** APK
2. **Open the app** and grant permissions
3. **Verify Termux integration** in the main dashboard

### 2. Install CLI Tools
1. **Navigate to CLI Tools** section
2. **Tap "Install"** on desired tools:
   - Claude CLI: `npm install -g @anthropics/claude-cli`
   - Cursor CLI: `npm install -g @cursor/cli`
   - HuggingFace CLI: `pip install huggingface_hub[cli]`
3. **Wait for installation** to complete

### 3. Start Using CLI Tools
1. **Tap "Activate"** on installed tools
2. **Tap "Open UI"** to launch chat interface
3. **Start chatting** with your CLI tools
4. **Browse files** in the File System section

## 🎯 Usage Examples

### Claude CLI Integration
```bash
# Through the app's chat interface
"Create a Python script to analyze CSV data"
"Explain this code snippet: [paste code]"
"Help me debug this error: [error message]"
```

### File System Management
- **Navigate directories** with breadcrumb navigation
- **View file types** with color-coded icons
- **Access artifacts** created by CLI tools
- **Export files** to external storage

### Session Management
- **Multiple sessions** for different CLI tools
- **Session history** with message persistence
- **Real-time status** monitoring
- **Background execution** support

## 🏗️ Technical Architecture

### Architecture Overview
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Android UI    │    │    Service      │    │     Termux      │
│   (Compose)     │◄──►│    Layer        │◄──►│   Environment   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ViewModels    │    │   Repository    │    │   CLI Tools     │
│   (MVVM)        │    │   Pattern       │    │   Integration   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Key Components
- **🎨 UI Layer**: Jetpack Compose with Material Design 3
- **🔄 Service Layer**: Background services for command execution
- **📦 Repository**: Centralized data management
- **🔌 Integration**: Direct Termux environment access

### Technologies Used
- **Kotlin**: Primary development language
- **Jetpack Compose**: Modern Android UI framework
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **StateFlow**: Reactive state management

## 🔧 Development

### Building Locally
```bash
# Clone the repository
git clone https://github.com/anthropics/termux-cli-manager.git
cd termux-cli-manager

# Install dependencies
./gradlew build

# Run tests
./gradlew test

# Build debug APK
./gradlew assembleDebug
```

### GitHub Actions CI/CD
The project uses GitHub Actions for:
- **🔨 Automated building** on every push
- **🧪 Running tests** and linting
- **📦 Creating releases** with APK artifacts
- **🔒 Security scanning** with SARIF reports

### Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Termux Team** - For the amazing Android terminal emulator
- **Anthropic** - For Claude CLI integration
- **Cursor Team** - For Cursor CLI support
- **HuggingFace** - For ML model management tools

## 📞 Support

- **🐛 Bug Reports**: [GitHub Issues](https://github.com/anthropics/termux-cli-manager/issues)
- **💡 Feature Requests**: [GitHub Discussions](https://github.com/anthropics/termux-cli-manager/discussions)
- **📖 Documentation**: [Wiki](https://github.com/anthropics/termux-cli-manager/wiki)

## 🗺️ Roadmap

- [ ] **Widget Support**: Home screen widgets for quick access
- [ ] **Cloud Sync**: Session backup and restore
- [ ] **Plugin System**: Support for custom CLI tools
- [ ] **Advanced File Editor**: Built-in code editor
- [ ] **Notification Integration**: Background task notifications
- [ ] **Shortcuts**: Android App Shortcuts support

---

<div align="center">
  <p>Made with ❤️ for the Android + Termux community</p>
  <p>
    <a href="https://github.com/anthropics/termux-cli-manager/releases">Download</a> •
    <a href="https://github.com/anthropics/termux-cli-manager/wiki">Documentation</a> •
    <a href="https://github.com/anthropics/termux-cli-manager/issues">Support</a>
  </p>
</div>