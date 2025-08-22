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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termux.climanager.ui.components.FileItemCard
import com.termux.climanager.ui.viewmodel.CLIManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSystemScreen(
    viewModel: CLIManagerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadRepositoryItems(uiState.currentPath)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("File System")
                        Text(
                            text = uiState.currentPath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.currentPath != "/") {
                        IconButton(onClick = { viewModel.navigateToParentDirectory() }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Parent Directory")
                        }
                    }
                    IconButton(onClick = { viewModel.loadRepositoryItems(uiState.currentPath) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.repositoryItems.isEmpty()) {
                // Empty directory
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Empty Directory",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "No files or folders found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Breadcrumb navigation
                    item {
                        BreadcrumbNavigation(
                            currentPath = uiState.currentPath,
                            onNavigateToPath = { path ->
                                viewModel.loadRepositoryItems(path)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Directory statistics
                    item {
                        DirectoryStatsCard(
                            totalItems = uiState.repositoryItems.size,
                            directories = uiState.repositoryItems.count { it.isDirectory },
                            files = uiState.repositoryItems.count { !it.isDirectory }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // File and folder items
                    items(uiState.repositoryItems) { item ->
                        FileItemCard(
                            item = item,
                            onClick = {
                                if (item.isDirectory) {
                                    viewModel.loadRepositoryItems(item.path)
                                } else {
                                    // TODO: Open file viewer
                                }
                            },
                            onLongClick = {
                                // TODO: Show file options menu
                            }
                        )
                    }
                }
            }
            
            // Error handling
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreadcrumbNavigation(
    currentPath: String,
    onNavigateToPath: (String) -> Unit
) {
    val pathParts = currentPath.split("/").filter { it.isNotEmpty() }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            
            TextButton(
                onClick = { onNavigateToPath("/") },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "root",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            pathParts.forEachIndexed { index, part ->
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                TextButton(
                    onClick = {
                        val targetPath = "/" + pathParts.take(index + 1).joinToString("/")
                        onNavigateToPath(targetPath)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = part,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (index == pathParts.lastIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun DirectoryStatsCard(
    totalItems: Int,
    directories: Int,
    files: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Folder,
                label = "Folders",
                count = directories
            )
            StatItem(
                icon = Icons.Default.InsertDriveFile,
                label = "Files", 
                count = files
            )
            StatItem(
                icon = Icons.Default.List,
                label = "Total",
                count = totalItems
            )
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}