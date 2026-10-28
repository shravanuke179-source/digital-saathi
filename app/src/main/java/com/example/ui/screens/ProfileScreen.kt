package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ui.components.rememberCloudAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val cloudAction = rememberCloudAction()
    val userProfile by repository.userProfile.collectAsState()
    val submittedApps by repository.submittedApplications.collectAsState()
    val supabaseStatus by repository.supabaseSyncStatus.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "supabase_sync_anim")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_rotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Dynamic Avatar (Photo for Ramesh Kumar, Personalized Initials Badge for Signed-In User)
                        val initials = userProfile.name
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .joinToString("")
                            .ifBlank { "U" }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (userProfile.authProvider.contains("Google", ignoreCase = true)) Color(0xFF1A73E8)
                                    else TerracottaPrimary
                                )
                                .border(2.dp, if (isDarkMode) Color.White.copy(alpha = 0.6f) else TerracottaPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfile.name.equals("Ramesh Kumar", ignoreCase = true)) {
                                Image(
                                    painter = painterResource(id = R.drawable.avatar_ramesh_kumar),
                                    contentDescription = "User Avatar",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (userProfile.isVerified) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = if (userProfile.isVerified) "Verified" else "Not verified",
                                    tint = if (userProfile.isVerified) ForestGreen else TextSecondaryMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${userProfile.village}, ${userProfile.district}",
                                fontSize = 12.sp,
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            )
                            Text(
                                text = userProfile.phone,
                                fontSize = 12.sp,
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            )
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = TerracottaPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Citizen Identity Details Row
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkMode) DarkBackground else Color(0xFFF9F7F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Email / Account:", fontSize = 11.sp, color = TextSecondaryMuted)
                                Text(userProfile.email, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Login Method:", fontSize = 11.sp, color = TextSecondaryMuted)
                                Text(userProfile.authProvider, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Aadhaar Status:", fontSize = 11.sp, color = TextSecondaryMuted)
                                Text(if (userProfile.isVerified) "Verified" else "Not verified", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Ration Card:", fontSize = 11.sp, color = TextSecondaryMuted)
                                Text(userProfile.rationCardNo, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("DBT Bank Account:", fontSize = 11.sp, color = TextSecondaryMuted)
                                Text(userProfile.bankLinked, fontSize = 11.sp, color = TextPrimaryDark)
                            }
                        }
                    }
                }
            }
        }

        // Supabase Database Cloud Sync Status Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) Color(0xFF1E2822) else Color(0xFFF1FBF4),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDarkMode) Color(0xFF2F4F38) else Color(0xFFC3E8CC)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supabase_sync_card")
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (supabaseStatus.isConnected) ForestGreen else AlertRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SUPABASE DATABASE (CLOUD SYNC)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDarkMode) Color(0xFF2B3A2F) else Color(0xFFE0F4E5)
                        ) {
                            Text(
                                text = "PostgreSQL Ready",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "User data is stored in Firebase Cloud Firestore at `${supabaseStatus.tableName}`.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Status: ${supabaseStatus.lastSyncMessage}",
                                fontSize = 11.sp,
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            )
                            Text(
                                text = "Last Synced: ${userProfile.lastSyncedWithSupabase}",
                                fontSize = 10.sp,
                                color = ForestGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { repository.syncWithSupabase() },
                            enabled = !supabaseStatus.isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .then(if (supabaseStatus.isSyncing) Modifier.rotate(syncRotation) else Modifier)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (supabaseStatus.isSyncing) "Syncing..." else "Sync Now",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // My Applications Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Submitted Applications (${submittedApps.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                )
            }
        }

        items(submittedApps) { app ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.appId,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoSecondary
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (app.status == "Approved") Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = app.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (app.status == "Approved") ForestGreen else AmberAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (selectedLanguage == AppLanguage.HINDI) app.schemeTitleHi else app.schemeTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { app.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TerracottaPrimary,
                        trackColor = if (isDarkMode) DarkCardBorder else CardBorderColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Applied: ${app.appliedDate}", fontSize = 10.sp, color = TextSecondaryMuted)
                        Text("Verification in progress", fontSize = 10.sp, color = ForestGreen, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Language Preferences Selection Card
        item {
            Text(
                text = "Language / भाषा / भाषा निवडा",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onLanguageChange(lang) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${lang.displayName} (${lang.nativeName})",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )

                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = IndigoSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Account Session & Sign Out Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) Color(0xFF2E1F1F) else Color(0xFFFFF5F5),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF5E3535) else Color(0xFFFFCDD2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Account Session (खाता सत्र)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = AlertRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Logged in as ${userProfile.email} (${userProfile.authProvider})",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { repository.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("logout_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Logout, contentDescription = "Log Out", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out / Switch Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(userProfile.name) }
        var villageInput by remember { mutableStateOf(userProfile.village) }
        var districtInput by remember { mutableStateOf(userProfile.district) }
        var phoneInput by remember { mutableStateOf(userProfile.phone) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Edit Profile Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = villageInput,
                        onValueChange = { villageInput = it },
                        label = { Text("Village (ग्राम)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = districtInput,
                        onValueChange = { districtInput = it },
                        label = { Text("District (जिला)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { cloudAction.launch {
                                repository.updateProfile(nameInput, villageInput, districtInput, phoneInput)
                                showEditProfileDialog = false
                            } },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save to Cloud")
                        }
                    }
                }
            }
        }
    }
}
