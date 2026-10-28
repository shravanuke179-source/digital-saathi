package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.model.Scheme
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun SchemeDetailDialog(
    scheme: Scheme,
    userProfile: UserProfile,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onBookmarkToggle: () -> Unit,
    onApplySubmitted: suspend (Scheme, String, String, String) -> String,
    onDismiss: () -> Unit
) {
    val cloudAction = rememberCloudAction()
    val context = LocalContext.current
    var isApplying by remember { mutableStateOf(false) }
    var applicationSubmitted by remember { mutableStateOf(false) }
    var generatedAppId by remember { mutableStateOf("") }

    var applicantName by remember { mutableStateOf(userProfile.name) }
    var applicantPhone by remember { mutableStateOf(userProfile.phone) }
    var applicantAadhaar by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Bar with Close and Bookmark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "● Active Scheme",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBookmarkToggle) {
                            Icon(
                                imageVector = if (scheme.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (scheme.isBookmarked) TerracottaPrimary else (if (isDarkMode) DarkTextSecondary else TextSecondaryMuted)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (applicationSubmitted) {
                    // Application success screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(44.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Application Saved!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ForestGreen
                        )
                        Text(
                            text = "आवेदन सहेजा गया। आधिकारिक पोर्टल पर जमा करें।",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) DarkBackground else WarmCreamBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Application ID: $generatedAppId", fontWeight = FontWeight.Bold, color = IndigoSecondary, fontSize = 14.sp)
                                Text("Scheme: ${scheme.titleEn}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Applicant: $applicantName", fontSize = 13.sp)
                                Text("Saved in Digital Saathi. Submit via the official portal.", fontSize = 13.sp, color = AmberAccent)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View in My Applications")
                        }
                    }
                } else if (isApplying) {
                    // Direct application form
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Apply: ${scheme.titleEn}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = applicantName,
                            onValueChange = { applicantName = it },
                            label = { Text("Full Name (नाम)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = applicantPhone,
                            onValueChange = { applicantPhone = it },
                            label = { Text("Mobile Number (मोबाइल)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = applicantAadhaar,
                            onValueChange = { applicantAadhaar = it },
                            label = { Text("Aadhaar last 4 digits (optional) (आधार)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8EDFF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Village: ${userProfile.village}, ${userProfile.district}",
                                fontSize = 12.sp,
                                color = IndigoSecondary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { cloudAction.launch {
                                generatedAppId = onApplySubmitted(scheme, applicantName, applicantPhone, applicantAadhaar)
                                applicationSubmitted = true
                            } },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_scheme_application_button")
                        ) {
                            Text("Submit Application Now", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { isApplying = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to Scheme Details")
                        }
                    }
                } else {
                    // Full Scheme Details
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) scheme.titleHi else scheme.titleEn,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) scheme.descriptionHi else scheme.descriptionEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Benefit Highlight Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF8E1),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFECB3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Official Benefit:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF795548))
                                    Text(
                                        text = if (selectedLanguage == AppLanguage.HINDI) scheme.benefitHi else scheme.benefitEn,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Eligibility Section
                        Text("Who Can Apply (पात्रता):", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        val eligibility = if (selectedLanguage == AppLanguage.HINDI) scheme.eligibilityHi else scheme.eligibilityEn
                        eligibility.forEach { item ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item, fontSize = 13.sp, color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Documents Section
                        Text("Required Documents (आवश्यक दस्तावेज़):", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        val docs = if (selectedLanguage == AppLanguage.HINDI) scheme.documentsHi else scheme.documentsEn
                        docs.forEach { doc ->
                            Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = IndigoSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(doc, fontSize = 13.sp, color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Application Process
                        Text("Application Process (आवेदन प्रक्रिया):", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) scheme.applicationProcessHi else scheme.applicationProcessEn,
                            fontSize = 13.sp,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Official Website Link
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme.officialLink))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // fallback
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Official Government Portal (${scheme.officialLink})", fontSize = 12.sp, maxLines = 1)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Apply Button
                        Button(
                            onClick = { isApplying = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("scheme_apply_now_button")
                        ) {
                            Text("Apply Now (आवेदन करें)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
