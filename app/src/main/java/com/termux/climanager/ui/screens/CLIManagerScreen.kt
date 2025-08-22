package com.termux.climanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termux.climanager.data.model.CLITool
import com.termux.climanager.ui.components.CLIToolCard
import com.termux.climanager.ui.viewmodel.CLIManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CLIManagerScreen(
    viewModel: CLIManagerViewModel,
    onNavigateToSessions: () -> Unit,
    onNavigateToFileSystem: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CLI Manager") },
                actions = {
                    IconButton(onClick = onNavigateToSessions) {
                        Icon(Icons.Default.Chat, contentDescription = "Sessions")
                    }
                    IconButton(onClick = onNavigateToFileSystem) {
                        Icon(Icons.Default.Folder, contentDescription = "File System")
                    }
                    IconButton(onClick = { viewModel.refreshCLIStatus() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatusCard(
                    title = "System Status",
                    content = {
                        Column {
                            StatusRow("Termux", true)
                            StatusRow("Service", uiState.isServiceRunning)
                            StatusRow("Active Sessions", uiState.activeSessions.isNotEmpty())
                        }
                    }
                )
            }
            
            item {
                Text(
                    text = "CLI Tools",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(uiState.cliTools) { tool ->
                CLIToolCard(
                    tool = tool,
                    onInstall = { viewModel.installCLITool(tool.name) },
                    onActivate = { viewModel.activateCLITool(tool.name) },
                    onDeactivate = { viewModel.deactivateCLITool(tool.name) },
                    onOpenUI = { viewModel.openCLIUI(tool.name) }
                )
            }
            
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            uiState.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun StatusRow(
    label: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isActive) Color.Green else Color.Red,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isActive) "Active" else "Inactive",
                color = if (isActive) Color.Green else Color.Red
            )
        }
    }
}