package com.termux.climanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.termux.climanager.R
import com.termux.climanager.data.model.CommandResult
import com.termux.climanager.data.model.CommandSession
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.*
import java.util.*
import javax.inject.Singleton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermuxCommandService : Service() {
    
    private val binder = TermuxCommandBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _activeSessions = MutableStateFlow<Map<String, CommandSession>>(emptyMap())
    val activeSessions: StateFlow<Map<String, CommandSession>> = _activeSessions
    
    private val _commandResults = MutableStateFlow<Map<String, CommandResult>>(emptyMap())
    val commandResults: StateFlow<Map<String, CommandResult>> = _commandResults
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "termux_cli_manager"
        private const val TERMUX_PREFIX = "/data/data/com.termux/files/usr"
        private const val TERMUX_HOME = "/data/data/com.termux/files/home"
    }
    
    inner class TermuxCommandBinder : Binder() {
        fun getService(): TermuxCommandService = this@TermuxCommandService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Termux CLI Manager",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Manages CLI tool execution and monitoring"
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CLI Manager Active")
            .setContentText("Managing CLI tools and sessions")
            .setSmallIcon(R.drawable.ic_terminal)
            .setOngoing(true)
            .build()
    }
    
    fun executeCommand(
        command: String,
        sessionId: String = UUID.randomUUID().toString(),
        workingDirectory: String = TERMUX_HOME
    ): String {
        return serviceScope.async {
            try {
                val session = CommandSession(
                    id = sessionId,
                    command = command,
                    workingDirectory = workingDirectory,
                    startTime = System.currentTimeMillis(),
                    isActive = true
                )
                
                updateSession(session)
                
                val result = executeTermuxCommand(command, workingDirectory)
                
                val finalSession = session.copy(
                    isActive = false,
                    endTime = System.currentTimeMillis()
                )
                updateSession(finalSession)
                
                updateCommandResult(sessionId, result)
                
                result.output
            } catch (e: Exception) {
                val errorResult = CommandResult(
                    sessionId = sessionId,
                    output = "",
                    error = e.message ?: "Unknown error",
                    exitCode = -1,
                    timestamp = System.currentTimeMillis()
                )
                updateCommandResult(sessionId, errorResult)
                throw e
            }
        }.let { 
            runBlocking { it.await() }
        }
    }
    
    suspend fun executeCommandAsync(
        command: String,
        sessionId: String = UUID.randomUUID().toString(),
        workingDirectory: String = TERMUX_HOME,
        onOutput: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): CommandResult = withContext(Dispatchers.IO) {
        val session = CommandSession(
            id = sessionId,
            command = command,
            workingDirectory = workingDirectory,
            startTime = System.currentTimeMillis(),
            isActive = true
        )
        
        updateSession(session)
        
        try {
            val result = executeTermuxCommandStreamed(command, workingDirectory, onOutput, onError)
            
            val finalSession = session.copy(
                isActive = false,
                endTime = System.currentTimeMillis()
            )
            updateSession(finalSession)
            
            updateCommandResult(sessionId, result)
            result
        } catch (e: Exception) {
            val errorResult = CommandResult(
                sessionId = sessionId,
                output = "",
                error = e.message ?: "Unknown error",
                exitCode = -1,
                timestamp = System.currentTimeMillis()
            )
            updateCommandResult(sessionId, errorResult)
            throw e
        }
    }
    
    private fun executeTermuxCommand(command: String, workingDirectory: String): CommandResult {
        val processBuilder = ProcessBuilder()
        
        // Set up Termux environment
        val env = processBuilder.environment()
        env["HOME"] = TERMUX_HOME
        env["PREFIX"] = TERMUX_PREFIX
        env["PATH"] = "$TERMUX_PREFIX/bin:$TERMUX_PREFIX/bin/applets"
        env["LD_LIBRARY_PATH"] = "$TERMUX_PREFIX/lib"
        env["TMPDIR"] = "$TERMUX_PREFIX/tmp"
        
        processBuilder.directory(File(workingDirectory))
        processBuilder.command("/system/bin/sh", "-c", command)
        
        val startTime = System.currentTimeMillis()
        val process = processBuilder.start()
        
        val output = StringBuilder()
        val error = StringBuilder()
        
        // Read output
        val outputReader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))
        
        val outputThread = Thread {
            outputReader.use { reader ->
                reader.forEachLine { line ->
                    output.appendLine(line)
                }
            }
        }
        
        val errorThread = Thread {
            errorReader.use { reader ->
                reader.forEachLine { line ->
                    error.appendLine(line)
                }
            }
        }
        
        outputThread.start()
        errorThread.start()
        
        val exitCode = process.waitFor()
        
        outputThread.join()
        errorThread.join()
        
        return CommandResult(
            sessionId = "",
            output = output.toString(),
            error = error.toString(),
            exitCode = exitCode,
            timestamp = startTime
        )
    }
    
    private suspend fun executeTermuxCommandStreamed(
        command: String,
        workingDirectory: String,
        onOutput: ((String) -> Unit)?,
        onError: ((String) -> Unit)?
    ): CommandResult = withContext(Dispatchers.IO) {
        val processBuilder = ProcessBuilder()
        
        // Set up Termux environment
        val env = processBuilder.environment()
        env["HOME"] = TERMUX_HOME
        env["PREFIX"] = TERMUX_PREFIX
        env["PATH"] = "$TERMUX_PREFIX/bin:$TERMUX_PREFIX/bin/applets"
        env["LD_LIBRARY_PATH"] = "$TERMUX_PREFIX/lib"
        env["TMPDIR"] = "$TERMUX_PREFIX/tmp"
        
        processBuilder.directory(File(workingDirectory))
        processBuilder.command("/system/bin/sh", "-c", command)
        
        val startTime = System.currentTimeMillis()
        val process = processBuilder.start()
        
        val output = StringBuilder()
        val error = StringBuilder()
        
        // Stream output in real-time
        val outputJob = async {
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.forEachLine { line ->
                    output.appendLine(line)
                    onOutput?.invoke(line)
                }
            }
        }
        
        val errorJob = async {
            BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                reader.forEachLine { line ->
                    error.appendLine(line)
                    onError?.invoke(line)
                }
            }
        }
        
        val exitCode = process.waitFor()
        
        outputJob.await()
        errorJob.await()
        
        CommandResult(
            sessionId = "",
            output = output.toString(),
            error = error.toString(),
            exitCode = exitCode,
            timestamp = startTime
        )
    }
    
    fun installCLITool(tool: String): String {
        return when (tool.lowercase()) {
            "claude" -> executeCommand("npm install -g @anthropics/claude-cli")
            "cursor" -> executeCommand("npm install -g @cursor/cli")
            "huggingface", "hf" -> executeCommand("pip install huggingface_hub[cli]")
            else -> throw IllegalArgumentException("Unknown CLI tool: $tool")
        }
    }
    
    fun checkCLIToolStatus(tool: String): Boolean {
        return try {
            val result = when (tool.lowercase()) {
                "claude" -> executeCommand("claude --version")
                "cursor" -> executeCommand("cursor --version")
                "huggingface", "hf" -> executeCommand("huggingface-cli --help")
                else -> return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun startCLISession(tool: String, sessionId: String): String {
        return when (tool.lowercase()) {
            "claude" -> {
                serviceScope.launch {
                    executeCommandAsync(
                        command = "claude code",
                        sessionId = sessionId,
                        onOutput = { /* Handle real-time output */ },
                        onError = { /* Handle real-time errors */ }
                    )
                }
                sessionId
            }
            "cursor" -> {
                serviceScope.launch {
                    executeCommandAsync(
                        command = "cursor .",
                        sessionId = sessionId,
                        onOutput = { /* Handle real-time output */ },
                        onError = { /* Handle real-time errors */ }
                    )
                }
                sessionId
            }
            else -> throw IllegalArgumentException("Unknown CLI tool: $tool")
        }
    }
    
    private fun updateSession(session: CommandSession) {
        val currentSessions = _activeSessions.value.toMutableMap()
        currentSessions[session.id] = session
        _activeSessions.value = currentSessions
    }
    
    private fun updateCommandResult(sessionId: String, result: CommandResult) {
        val currentResults = _commandResults.value.toMutableMap()
        currentResults[sessionId] = result.copy(sessionId = sessionId)
        _commandResults.value = currentResults
    }
    
    fun getSessionOutput(sessionId: String): CommandResult? {
        return _commandResults.value[sessionId]
    }
    
    fun terminateSession(sessionId: String) {
        val currentSessions = _activeSessions.value.toMutableMap()
        currentSessions[sessionId]?.let { session ->
            currentSessions[sessionId] = session.copy(isActive = false, endTime = System.currentTimeMillis())
            _activeSessions.value = currentSessions
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}