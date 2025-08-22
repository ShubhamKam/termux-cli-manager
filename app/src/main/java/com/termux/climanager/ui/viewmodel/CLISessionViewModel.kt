package com.termux.climanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.termux.climanager.data.model.ChatMessage
import com.termux.climanager.data.model.FileAttachment
import com.termux.climanager.repository.CLIManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CLISessionUiState(
    val sessionId: String = "",
    val toolName: String = "",
    val sessionTitle: String = "",
    val isActive: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CLISessionViewModel @Inject constructor(
    private val repository: CLIManagerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CLISessionUiState())
    val uiState: StateFlow<CLISessionUiState> = _uiState.asStateFlow()
    
    fun initialize(toolName: String, sessionId: String?) {
        viewModelScope.launch {
            val actualSessionId = sessionId ?: repository.createNewChatSession(toolName)
            
            _uiState.value = _uiState.value.copy(
                sessionId = actualSessionId,
                toolName = toolName,
                sessionTitle = "${toolName.capitalize()} CLI Session",
                isActive = true
            )
            
            // Observe session changes
            repository.observeActiveSessions().collect { sessions ->
                sessions[actualSessionId]?.let { session ->
                    _uiState.value = _uiState.value.copy(
                        messages = session.messages,
                        isActive = session.isActive,
                        sessionTitle = session.title
                    )
                }
            }
        }
    }
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                repository.sendMessage(_uiState.value.sessionId, message)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to send message: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun toggleSession() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (currentState.isActive) {
                repository.terminateSession(currentState.sessionId)
            } else {
                try {
                    repository.activateCLITool(currentState.toolName)
                    _uiState.value = _uiState.value.copy(isActive = true)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to activate session: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearMessages() {
        viewModelScope.launch {
            // Create a new session to clear messages
            val newSessionId = repository.createNewChatSession(_uiState.value.toolName)
            
            // Terminate the old session
            repository.terminateSession(_uiState.value.sessionId)
            
            _uiState.value = _uiState.value.copy(
                sessionId = newSessionId,
                messages = emptyList(),
                error = null
            )
        }
    }
    
    fun openFile(file: FileAttachment) {
        // TODO: Implement file opening logic
        viewModelScope.launch {
            try {
                // This could open a file viewer or trigger a download
                // For now, we'll just show a message
                _uiState.value = _uiState.value.copy(
                    error = "File viewing not yet implemented: ${file.name}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to open file: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}