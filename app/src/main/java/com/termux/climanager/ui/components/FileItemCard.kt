package com.termux.climanager.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.termux.climanager.data.model.FileType
import com.termux.climanager.data.model.RepositoryItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemCard(
    item: RepositoryItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File/Folder icon
            Icon(
                imageVector = getItemIcon(item),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = getItemIconColor(item)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // File info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.isDirectory) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item.isDirectory) "Folder" else formatFileSize(item.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (!item.isDirectory) {
                        Text(
                            text = " • ${item.type.name.lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = dateFormat.format(Date(item.lastModified)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Arrow for directories
            if (item.isDirectory) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getItemIcon(item: RepositoryItem): androidx.compose.ui.graphics.vector.ImageVector {
    return if (item.isDirectory) {
        Icons.Default.Folder
    } else {
        when (item.type) {
            FileType.CODE -> Icons.Default.Code
            FileType.TEXT -> Icons.Default.Description
            FileType.IMAGE -> Icons.Default.Image
            FileType.AUDIO -> Icons.Default.AudioFile
            FileType.VIDEO -> Icons.Default.VideoFile
            FileType.ARCHIVE -> Icons.Default.Archive
            FileType.EXECUTABLE -> Icons.Default.Launch
            FileType.UNKNOWN -> Icons.Default.InsertDriveFile
        }
    }
}

@Composable
private fun getItemIconColor(item: RepositoryItem): Color {
    return if (item.isDirectory) {
        MaterialTheme.colorScheme.primary
    } else {
        when (item.type) {
            FileType.CODE -> Color(0xFF4CAF50) // Green
            FileType.TEXT -> Color(0xFF2196F3) // Blue
            FileType.IMAGE -> Color(0xFFFF9800) // Orange
            FileType.AUDIO -> Color(0xFF9C27B0) // Purple
            FileType.VIDEO -> Color(0xFFE91E63) // Pink
            FileType.ARCHIVE -> Color(0xFF795548) // Brown
            FileType.EXECUTABLE -> Color(0xFFF44336) // Red
            FileType.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return if (unitIndex == 0) {
        "${size.toInt()} ${units[unitIndex]}"
    } else {
        "%.1f %s".format(size, units[unitIndex])
    }
}