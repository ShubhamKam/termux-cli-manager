package com.termux.climanager.ui.components

import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CLIToolCard(
    tool: CLITool,
    onInstall: () -> Unit,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onOpenUI: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tool.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    tool.version?.let { version ->
                        Text(
                            text = "v$version",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Installation status
                    Icon(
                        imageVector = if (tool.isInstalled) Icons.Default.CheckCircle else Icons.Default.Download,
                        contentDescription = null,
                        tint = if (tool.isInstalled) Color.Green else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Active status
                    if (tool.isInstalled) {
                        Icon(
                            imageVector = if (tool.isActive) Icons.Default.PlayArrow else Icons.Default.Stop,
                            contentDescription = null,
                            tint = if (tool.isActive) Color.Green else Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status text
            Text(
                text = when {
                    !tool.isInstalled -> "Not installed"
                    tool.isActive -> "Active and running"
                    else -> "Installed but not active"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    !tool.isInstalled -> MaterialTheme.colorScheme.onSurfaceVariant
                    tool.isActive -> Color.Green
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!tool.isInstalled) {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install")
                    }
                } else {
                    // Activate/Deactivate button
                    if (tool.isActive) {
                        OutlinedButton(
                            onClick = onDeactivate,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deactivate")
                        }
                    } else {
                        Button(
                            onClick = onActivate,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activate")
                        }
                    }
                    
                    // Open UI button
                    if (tool.isActive && tool.name.lowercase() in listOf("claude", "cursor")) {
                        Button(
                            onClick = onOpenUI,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Launch,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open UI")
                        }
                    }
                }
            }
        }
    }
}