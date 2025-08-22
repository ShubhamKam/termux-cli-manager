package com.termux.climanager.data.model

data class CommandResult(
    val sessionId: String,
    val output: String,
    val error: String,
    val exitCode: Int,
    val timestamp: Long
)

data class CommandSession(
    val id: String,
    val command: String,
    val workingDirectory: String,
    val startTime: Long,
    val endTime: Long? = null,
    val isActive: Boolean = true
)

data class CLITool(
    val name: String,
    val displayName: String,
    val installCommand: String,
    val checkCommand: String,
    val startCommand: String,
    val isInstalled: Boolean = false,
    val isActive: Boolean = false,
    val version: String? = null
)

data class ChatSession(
    val id: String,
    val toolName: String,
    val title: String,
    val createdAt: Long,
    val lastActivity: Long,
    val isActive: Boolean = false,
    val messages: List<ChatMessage> = emptyList()
)

data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val attachments: List<FileAttachment> = emptyList()
)

data class FileAttachment(
    val name: String,
    val path: String,
    val type: String,
    val size: Long
)

data class RepositoryItem(
    val name: String,
    val path: String,
    val type: FileType,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean
)

enum class FileType {
    CODE,
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO,
    ARCHIVE,
    EXECUTABLE,
    UNKNOWN
}