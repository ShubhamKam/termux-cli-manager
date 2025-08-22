package com.termux.climanager.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.termux.climanager.data.model.*
import com.termux.climanager.service.TermuxCommandService
import com.termux.climanager.ui.session.CLISessionActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CLIManagerRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var commandService: TermuxCommandService? = null
    private var isServiceBound = false
    
    private val _activeSessions = MutableStateFlow<Map<String, ChatSession>>(emptyMap())
    private val _serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TermuxCommandService.TermuxCommandBinder
            commandService = binder.getService()
            isServiceBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            commandService = null
            isServiceBound = false
        }
    }
    
    init {
        bindToCommandService()
    }
    
    private fun bindToCommandService() {
        val intent = Intent(context, TermuxCommandService::class.java)
        context.startService(intent)
        context.bindService(intent, _serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    fun isServiceRunning(): Boolean = isServiceBound && commandService != null
    
    suspend fun installCLITool(toolName: String): String {
        return commandService?.installCLITool(toolName)
            ?: throw IllegalStateException("Command service not available")
    }
    
    fun checkCLIToolStatus(toolName: String): Boolean {
        return commandService?.checkCLIToolStatus(toolName) ?: false
    }
    
    fun getCLIToolVersion(toolName: String): String? {
        return try {
            when (toolName.lowercase()) {
                "claude" -> {
                    val result = commandService?.executeCommand("claude --version")
                    result?.takeIf { it.contains("claude") }?.split(" ")?.lastOrNull()
                }
                "cursor" -> {
                    val result = commandService?.executeCommand("cursor --version")
                    result?.takeIf { it.isNotBlank() }?.trim()
                }
                "huggingface" -> {
                    val result = commandService?.executeCommand("python -c \"import huggingface_hub; print(huggingface_hub.__version__)\"")
                    result?.takeIf { it.isNotBlank() }?.trim()
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun isCLIToolActive(toolName: String): Boolean {
        val currentSessions = _activeSessions.value
        return currentSessions.values.any { it.toolName == toolName && it.isActive }
    }
    
    fun activateCLITool(toolName: String): String {
        val sessionId = UUID.randomUUID().toString()
        return commandService?.startCLISession(toolName, sessionId)
            ?: throw IllegalStateException("Command service not available")
    }
    
    fun deactivateCLITool(toolName: String) {
        val currentSessions = _activeSessions.value.toMutableMap()
        currentSessions.entries.removeAll { (_, session) ->
            if (session.toolName == toolName && session.isActive) {
                commandService?.terminateSession(session.id)
                true
            } else false
        }
        _activeSessions.value = currentSessions
    }
    
    fun openCLIUI(toolName: String) {
        val intent = Intent(context, CLISessionActivity::class.java).apply {
            putExtra("tool_name", toolName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    
    fun createNewChatSession(toolName: String): String {
        val sessionId = UUID.randomUUID().toString()
        val session = ChatSession(
            id = sessionId,
            toolName = toolName,
            title = "New ${toolName.capitalize()} Session",
            createdAt = System.currentTimeMillis(),
            lastActivity = System.currentTimeMillis(),
            isActive = true
        )
        
        val currentSessions = _activeSessions.value.toMutableMap()
        currentSessions[sessionId] = session
        _activeSessions.value = currentSessions
        
        return sessionId
    }
    
    suspend fun sendMessage(sessionId: String, message: String) {
        val session = _activeSessions.value[sessionId]
            ?: throw IllegalArgumentException("Session not found: $sessionId")
        
        val chatMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = message,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        
        val updatedMessages = session.messages + chatMessage
        val updatedSession = session.copy(
            messages = updatedMessages,
            lastActivity = System.currentTimeMillis()
        )
        
        val currentSessions = _activeSessions.value.toMutableMap()
        currentSessions[sessionId] = updatedSession
        _activeSessions.value = currentSessions
        
        // Execute the command through the CLI tool
        commandService?.executeCommandAsync(
            command = message,
            sessionId = sessionId,
            onOutput = { output ->
                handleCLIOutput(sessionId, output, false)
            },
            onError = { error ->
                handleCLIOutput(sessionId, error, true)
            }
        )
    }
    
    private fun handleCLIOutput(sessionId: String, output: String, isError: Boolean) {
        val session = _activeSessions.value[sessionId] ?: return
        
        val responseMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = output,
            isUser = false,
            timestamp = System.currentTimeMillis()
        )
        
        val updatedMessages = session.messages + responseMessage
        val updatedSession = session.copy(
            messages = updatedMessages,
            lastActivity = System.currentTimeMillis()
        )
        
        val currentSessions = _activeSessions.value.toMutableMap()
        currentSessions[sessionId] = updatedSession
        _activeSessions.value = currentSessions
    }
    
    fun observeActiveSessions(): Flow<Map<String, ChatSession>> = _activeSessions.asStateFlow()
    
    fun getRepositoryItems(path: String): List<RepositoryItem> {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            throw IllegalArgumentException("Invalid directory path: $path")
        }
        
        return directory.listFiles()?.map { file ->
            RepositoryItem(
                name = file.name,
                path = file.absolutePath,
                type = getFileType(file),
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified(),
                isDirectory = file.isDirectory
            )
        }?.sortedWith(compareBy<RepositoryItem> { !it.isDirectory }.thenBy { it.name })
            ?: emptyList()
    }
    
    private fun getFileType(file: File): FileType {
        if (file.isDirectory) return FileType.UNKNOWN
        
        return when (file.extension.lowercase()) {
            "kt", "java", "js", "ts", "py", "cpp", "c", "h", "swift", "go", "rs" -> FileType.CODE
            "txt", "md", "json", "xml", "yml", "yaml", "conf", "ini" -> FileType.TEXT
            "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp" -> FileType.IMAGE
            "mp3", "wav", "ogg", "m4a", "flac" -> FileType.AUDIO
            "mp4", "avi", "mkv", "mov", "wmv", "webm" -> FileType.VIDEO
            "zip", "tar", "gz", "rar", "7z", "deb", "apk" -> FileType.ARCHIVE
            "exe", "bin", "app", "deb", "run", "sh" -> FileType.EXECUTABLE
            else -> FileType.UNKNOWN
        }
    }
    
    fun getChatSession(sessionId: String): ChatSession? {
        return _activeSessions.value[sessionId]
    }
    
    fun terminateSession(sessionId: String) {
        commandService?.terminateSession(sessionId)
        val currentSessions = _activeSessions.value.toMutableMap()
        currentSessions[sessionId]?.let { session ->
            currentSessions[sessionId] = session.copy(isActive = false)
        }
        _activeSessions.value = currentSessions
    }
    
    fun cleanup() {
        if (isServiceBound) {
            context.unbindService(_serviceConnection)
            isServiceBound = false
        }
    }
}