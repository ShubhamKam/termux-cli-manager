package com.termux.climanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termux.climanager.data.model.ChatMessage
import com.termux.climanager.data.model.FileAttachment
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatMessageCard(
    message: ChatMessage,
    onFileClick: (FileAttachment) -> Unit
) {
    val isUser = message.isUser
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Bot avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Message content
                if (message.content.isNotBlank()) {
                    if (isCodeBlock(message.content)) {
                        CodeBlock(
                            code = extractCode(message.content),
                            language = extractLanguage(message.content)
                        )
                    } else {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUser) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // File attachments
                if (message.attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(message.attachments) { attachment ->
                            FileAttachmentChip(
                                attachment = attachment,
                                onClick = { onFileClick(attachment) }
                            )
                        }
                    }
                }
                
                // Timestamp
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUser) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun CodeBlock(
    code: String,
    language: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (language != null && language.isNotBlank()) {
                Text(
                    text = language,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FileAttachmentChip(
    attachment: FileAttachment,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { 
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = getFileIcon(attachment.type),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

private fun getFileIcon(fileType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (fileType.lowercase()) {
        "image", "png", "jpg", "jpeg", "gif" -> Icons.Default.Image
        "code", "kt", "java", "js", "py" -> Icons.Default.Code
        "text", "txt", "md" -> Icons.Default.Description
        "audio", "mp3", "wav" -> Icons.Default.AudioFile
        "video", "mp4", "avi" -> Icons.Default.VideoFile
        "archive", "zip", "tar" -> Icons.Default.Archive
        else -> Icons.Default.InsertDriveFile
    }
}

private fun isCodeBlock(content: String): Boolean {
    return content.trimStart().startsWith("```")
}

private fun extractCode(content: String): String {
    val lines = content.lines()
    if (lines.isEmpty()) return content
    
    val startIndex = lines.indexOfFirst { it.trimStart().startsWith("```") }
    val endIndex = lines.indexOfLast { it.trimStart().startsWith("```") }
    
    return if (startIndex != -1 && endIndex != -1 && startIndex != endIndex) {
        lines.subList(startIndex + 1, endIndex).joinToString("\n")
    } else {
        content
    }
}

private fun extractLanguage(content: String): String? {
    val firstLine = content.lines().firstOrNull()?.trimStart()
    return if (firstLine?.startsWith("```") == true) {
        firstLine.substring(3).trim().takeIf { it.isNotBlank() }
    } else {
        null
    }
}