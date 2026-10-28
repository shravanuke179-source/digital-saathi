package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.components.GrievanceDialog
import com.example.ui.components.PostNoticeDialog
import com.example.ui.theme.*

@Composable
fun VillageSosScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notices by repository.notices.collectAsState()
    val grievances by repository.grievances.collectAsState()
    val emergencyContacts = repository.emergencyContacts
    val userProfile by repository.userProfile.collectAsState()

    var showGrievanceDialog by remember { mutableStateOf(false) }
    var showPostNoticeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Village Header Banner matching Screenshot 2
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) Color(0xFF282538) else Color(0xFFF3F4FD),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF3F466E) else Color(0xFFD6DBF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Village Portal • Palampur",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = IndigoSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Official announcements, Gram Sabha updates & community helpline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showGrievanceDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("file_grievance_banner_button")
                    ) {
                        Icon(Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("File Panchayat Grievance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Emergency SOS Direct Lines
        item {
            Column {
                Text(
                    text = "Emergency SOS Direct Lines (आपातकालीन नंबर)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to call directly from your phone",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                )
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(emergencyContacts) { contact ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                        modifier = Modifier
                            .width(135.dp)
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.number}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // fallback
                                }
                            }
                            .testTag("sos_button_${contact.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(contact.colorHex).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (contact.iconType) {
                                        "police" -> Icons.Default.LocalPolice
                                        "ambulance" -> Icons.Default.MedicalServices
                                        "women" -> Icons.Default.SupportAgent
                                        "kisan" -> Icons.Default.PhoneCallback
                                        else -> Icons.Default.HealthAndSafety
                                    },
                                    contentDescription = null,
                                    tint = Color(contact.colorHex),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = contact.nameEn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = contact.number,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(contact.colorHex)
                            )
                        }
                    }
                }
            }
        }

        // Gram Panchayat Notice Board matching Screenshot 2
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gram Panchayat Notice Board",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                )

                OutlinedButton(
                    onClick = { showPostNoticeDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("open_post_notice_button")
                ) {
                    Text("+ Post Notice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                }
            }
        }

        // Notices List matching Screenshot 2
        items(notices) { notice ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notice_item_${notice.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (notice.iconType) {
                                            "water" -> Color(0xFFE0F7FA)
                                            "health" -> Color(0xFFFCE4EC)
                                            "meeting" -> Color(0xFFEDE7F6)
                                            else -> Color(0xFFFFF3E0)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (notice.iconType) {
                                        "water" -> Icons.Default.WaterDrop
                                        "health" -> Icons.Default.LocalHospital
                                        "meeting" -> Icons.Default.Groups
                                        else -> Icons.Default.Campaign
                                    },
                                    contentDescription = null,
                                    tint = when (notice.iconType) {
                                        "water" -> Color(0xFF00838F)
                                        "health" -> Color(0xFFC2185B)
                                        "meeting" -> IndigoSecondary
                                        else -> AmberAccent
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = if (selectedLanguage == AppLanguage.HINDI) notice.titleHi else notice.titleEn,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )
                        }

                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) notice.timestampHi else notice.timestampEn,
                            fontSize = 11.sp,
                            color = if (isDarkMode) DarkTextSecondary else TextSubtle
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (selectedLanguage == AppLanguage.HINDI) notice.descriptionHi else notice.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Authority: ${if (selectedLanguage == AppLanguage.HINDI) notice.authorityHi else notice.authorityEn}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TerracottaPrimary
                        )

                        Text(
                            text = "Official",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                }
            }
        }

        // User Grievances tracking (if any)
        if (grievances.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "My Submitted Grievances (${grievances.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                )
            }

            items(grievances) { g ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) DarkCardSurface else WarmCreamBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(g.id, fontWeight = FontWeight.Bold, color = TerracottaPrimary, fontSize = 13.sp)
                            Text(g.date, fontSize = 11.sp, color = TextSubtle)
                        }
                        Text(g.subject, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Category: ${g.category}", fontSize = 11.sp, color = TextSecondaryMuted)
                        Text("Status: ${g.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                    }
                }
            }
        }
    }

    if (showGrievanceDialog) {
        GrievanceDialog(
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onSubmit = { cat, sub, desc ->
                repository.submitGrievance(cat, sub, desc).id
            },
            onDismiss = { showGrievanceDialog = false }
        )
    }

    if (showPostNoticeDialog) {
        PostNoticeDialog(
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onPostNotice = { title, desc, auth, cat ->
                repository.addVillageNotice(title, desc, auth, cat)
            },
            onDismiss = { showPostNoticeDialog = false }
        )
    }
}
