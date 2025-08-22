package com.termux.climanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.termux.climanager.data.model.*
import com.termux.climanager.repository.CLIManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CLIManagerUiState(
    val cliTools: List<CLITool> = emptyList(),
    val activeSessions: Map<String, ChatSession> = emptyMap(),
    val repositoryItems: List<RepositoryItem> = emptyList(),
    val currentPath: String = "/data/data/com.termux/files/home",
    val isServiceRunning: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CLIManagerViewModel @Inject constructor(
    private val repository: CLIManagerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CLIManagerUiState())
    val uiState: StateFlow<CLIManagerUiState> = _uiState.asStateFlow()
    
    init {
        initializeViewModel()
    }
    
    private fun initializeViewModel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Initialize CLI tools
                val cliTools = listOf(
                    CLITool(
                        name = "claude",
                        displayName = "Claude CLI",
                        installCommand = "npm install -g @anthropics/claude-cli",
                        checkCommand = "claude --version",
                        startCommand = "claude code"
                    ),
                    CLITool(
                        name = "cursor",
                        displayName = "Cursor CLI",
                        installCommand = "npm install -g @cursor/cli",
                        checkCommand = "cursor --version",
                        startCommand = "cursor ."
                    ),
                    CLITool(
                        name = "huggingface",
                        displayName = "HuggingFace CLI",
                        installCommand = "pip install huggingface_hub[cli]",
                        checkCommand = "huggingface-cli --help",
                        startCommand = "huggingface-cli"
                    )
                )
                
                // Check installation status for each tool
                val updatedTools = cliTools.map { tool ->
                    val isInstalled = repository.checkCLIToolStatus(tool.name)
                    val version = if (isInstalled) repository.getCLIToolVersion(tool.name) else null
                    tool.copy(
                        isInstalled = isInstalled,
                        version = version
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    cliTools = updatedTools,
                    isServiceRunning = repository.isServiceRunning(),
                    isLoading = false
                )
                
                // Start observing active sessions
                observeActiveSessions()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    private fun observeActiveSessions() {
        viewModelScope.launch {
            repository.observeActiveSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(activeSessions = sessions)
            }
        }
    }
    
    fun refreshCLIStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val updatedTools = _uiState.value.cliTools.map { tool ->
                    val isInstalled = repository.checkCLIToolStatus(tool.name)
                    val isActive = repository.isCLIToolActive(tool.name)
                    val version = if (isInstalled) repository.getCLIToolVersion(tool.name) else null
                    
                    tool.copy(
                        isInstalled = isInstalled,
                        isActive = isActive,
                        version = version
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    cliTools = updatedTools,
                    isServiceRunning = repository.isServiceRunning(),
                    isLoading = false,
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun installCLITool(toolName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                repository.installCLITool(toolName)
                refreshCLIStatus()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to install $toolName: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun activateCLITool(toolName: String) {
        viewModelScope.launch {
            try {
                repository.activateCLITool(toolName)
                refreshCLIStatus()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to activate $toolName: ${e.message}"
                )
            }
        }
    }
    
    fun deactivateCLITool(toolName: String) {
        viewModelScope.launch {
            try {
                repository.deactivateCLITool(toolName)
                refreshCLIStatus()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to deactivate $toolName: ${e.message}"
                )
            }
        }
    }
    
    fun openCLIUI(toolName: String) {
        viewModelScope.launch {
            try {
                repository.openCLIUI(toolName)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to open UI for $toolName: ${e.message}"
                )
            }
        }
    }
    
    fun createNewChatSession(toolName: String): String {
        return repository.createNewChatSession(toolName)
    }
    
    fun sendMessage(sessionId: String, message: String) {
        viewModelScope.launch {
            try {
                repository.sendMessage(sessionId, message)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to send message: ${e.message}"
                )
            }
        }
    }
    
    fun loadRepositoryItems(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val items = repository.getRepositoryItems(path)
                _uiState.value = _uiState.value.copy(
                    repositoryItems = items,
                    currentPath = path,
                    isLoading = false,
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load directory: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun navigateToParentDirectory() {
        val currentPath = _uiState.value.currentPath
        val parentPath = currentPath.substringBeforeLast("/").takeIf { it.isNotEmpty() } ?: "/"
        loadRepositoryItems(parentPath)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}