# Termux CLI Manager - Setup Guide

## Overview
This Android native service integrates with Termux to provide a unified interface for managing CLI tools like Claude CLI, Cursor CLI, and HuggingFace CLI through the Android Settings.

## Features

### Core Functionality
- **Settings Integration**: Access from Android Settings → Apps → Termux CLI Manager
- **CLI Tool Management**: Install, activate, and monitor Claude CLI, Cursor CLI, and HuggingFace CLI
- **Live Chat Interface**: Direct interaction with CLI tools through a chat-based UI
- **File System Browser**: Access and view Termux file system with artifact support
- **Session Management**: Multiple concurrent CLI sessions with real-time status
- **Command Execution**: Direct Termux command execution with live output streaming

### UI Components
- **Main Dashboard**: Overview of all CLI tools with installation/activation status
- **Chat Sessions**: Real-time communication with CLI tools
- **File Repository**: Browse and access created files and artifacts
- **Settings Panel**: Configure CLI tools and manage sessions

## Installation Steps

### 1. Prerequisites
```bash
# Ensure you have Node.js and Python in Termux
pkg update && pkg upgrade
pkg install nodejs python git

# Install required development tools
npm install -g @angular/cli
pip install --upgrade pip
```

### 2. Build the Android App
```bash
cd TermuxCLIManager

# Build the APK
./gradlew assembleDebug

# Install the APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3. Grant Permissions
The app requires the following permissions:
- Storage access (for file system browsing)
- Network access (for CLI tool installations)
- Foreground service (for background command execution)

### 4. CLI Tool Installation
Using the app interface:

#### Claude CLI
1. Open Termux CLI Manager
2. Tap "Install" on Claude CLI card
3. Wait for installation to complete
4. Tap "Activate" to start Claude CLI service
5. Tap "Open UI" to launch the chat interface

#### Cursor CLI
1. Tap "Install" on Cursor CLI card
2. Installation via npm
3. Activate and use through the UI

#### HuggingFace CLI
1. Tap "Install" on HuggingFace CLI card
2. Installation via pip
3. Access through the chat interface

## Usage Guide

### Managing CLI Tools
1. **Installation Status**: Green checkmark indicates installed tools
2. **Active Status**: Play/Stop icon shows current activation state
3. **Version Display**: Shows installed version information
4. **Quick Actions**: Install, activate, deactivate, and open UI buttons

### Chat Interface
1. **Start Session**: Tap "Open UI" on any active CLI tool
2. **Send Commands**: Type commands in the message input field
3. **View Responses**: Real-time output display with syntax highlighting
4. **File Attachments**: Generated files appear as clickable attachments
5. **Session Management**: Start/stop, clear, and manage multiple sessions

### File System Browser
1. **Navigate**: Tap folders to explore directory structure
2. **File Info**: View file type, size, and modification date
3. **Quick Access**: Breadcrumb navigation for easy traversal
4. **File Types**: Color-coded icons for different file types

### Settings Integration
1. Access via Android Settings → Apps → Termux CLI Manager
2. Configure CLI tool preferences
3. Manage background service settings
4. View system status and logs

## Technical Architecture

### Service Layer
- **TermuxCommandService**: Background service for command execution
- **CLIStatusService**: Monitors CLI tool status and availability
- **Repository Pattern**: Centralized data management

### UI Layer
- **Jetpack Compose**: Modern Android UI framework
- **MVVM Architecture**: Clean separation of concerns
- **Hilt Dependency Injection**: Efficient dependency management

### Integration
- **Termux Environment**: Direct command execution in Termux context
- **File Provider**: Secure file access across app boundaries
- **Background Processing**: Foreground service for long-running tasks

## Troubleshooting

### Common Issues

#### CLI Tool Installation Fails
- Check network connection
- Verify Termux has necessary packages (nodejs, python)
- Check storage permissions

#### Session Not Starting
- Ensure CLI tool is properly installed
- Check if Termux service is running
- Verify app permissions

#### File Access Issues
- Grant storage permissions
- Check Termux file access rights
- Verify file provider configuration

### Debug Information
Access debug logs through:
1. Android Settings → Apps → Termux CLI Manager → Storage
2. Check app logs in `/data/data/com.termux.climanager/`
3. Monitor Termux logs with `logcat`

## Development Notes

### Building from Source
```bash
# Clone and build
git clone <repository-url>
cd TermuxCLIManager

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test
```

### Architecture Decisions
- **Kotlin**: Primary development language
- **Compose**: Modern UI framework
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **StateFlow**: Reactive state management

### Security Considerations
- File access through secure providers
- Command execution in isolated environment
- No direct access to sensitive system areas
- Permissions-based security model

## Future Enhancements
- Additional CLI tools support
- Enhanced file viewer with syntax highlighting
- Export/import session configurations
- Advanced command history and templates
- Integration with cloud storage services

## Support
For issues and feature requests, please check the documentation or create an issue in the project repository.