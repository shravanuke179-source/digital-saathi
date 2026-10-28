package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.ui.theme.*

@Composable
fun PostNoticeDialog(
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onPostNotice: suspend (title: String, description: String, authority: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    val cloudAction = rememberCloudAction()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var authority by remember { mutableStateOf("Gram Panchayat Office") }
    var category by remember { mutableStateOf("meeting") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+ Post Village Notice",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title (शीर्षक)") },
                    placeholder = { Text("e.g. Clean Village Drive on Sunday") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notice Details (सूचना विवरण)") },
                    placeholder = { Text("Time, place, instructions for villagers...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = authority,
                    onValueChange = { authority = it },
                    label = { Text("Issuing Authority (जारीकर्ता)") },
                    placeholder = { Text("e.g. Gram Panchayat Office / PHC") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { cloudAction.launch {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            onPostNotice(title, description, authority, category)
                            onDismiss()
                        }
                    } },
                    enabled = title.isNotBlank() && description.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("post_notice_submit_button")
                ) {
                    Text("Publish Notice to Board", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
