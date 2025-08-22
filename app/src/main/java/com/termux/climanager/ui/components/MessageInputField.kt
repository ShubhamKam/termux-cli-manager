package com.termux.climanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputField(
    onSendMessage: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    val isMessageValid = textFieldValue.text.isNotBlank()
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                placeholder = { 
                    Text(
                        text = if (enabled) "Type a command or message..." else "Session inactive",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 120.dp),
                enabled = enabled,
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                trailingIcon = if (textFieldValue.text.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = { textFieldValue = TextFieldValue() }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                } else null
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = {
                    if (isMessageValid) {
                        onSendMessage(textFieldValue.text)
                        textFieldValue = TextFieldValue()
                    }
                },
                modifier = Modifier.size(56.dp),
                containerColor = if (enabled && isMessageValid) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (enabled && isMessageValid) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = if (enabled && isMessageValid) Icons.Default.Send else Icons.Default.Block,
                    contentDescription = "Send message"
                )
            }
        }
    }
}