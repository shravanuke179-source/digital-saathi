package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.NavDestination
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun DigitalSaathiTopBar(
    currentDestination: NavDestination,
    selectedLanguage: AppLanguage,
    userProfile: UserProfile,
    isDarkMode: Boolean,
    onDestinationSelected: (NavDestination) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleDarkMode: () -> Unit,
    onVoiceAssistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = if (isDarkMode) DarkCardSurface else TopBarBackground,
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isDarkMode) DarkCardBorder else CardBorderColor
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Top branding and action controls row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Emblem & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable { onDestinationSelected(NavDestination.HOME) }
                        .testTag("brand_header")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDarkMode) TerracottaPrimaryDark else Color(0xFFFAF0E6))
                            .border(1.dp, Color(0xFFE2C4A2), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Emblem",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "भारत पोर्टल",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) TerracottaContainer else TerracottaPrimary,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Digital Saathi • Citizen Services",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 10.sp,
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action Controls: Voice, Theme, Language, Profile Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Voice Reader Assistance
                    IconButton(
                        onClick = onVoiceAssistClick,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("tts_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VolumeUp,
                            contentDescription = "Audio read aloud",
                            tint = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Dark / Light Toggle
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Language Selector
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDarkMode) DarkBackground else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDarkMode) DarkCardBorder else CardBorderColor
                            ),
                            modifier = Modifier
                                .clickable { languageMenuExpanded = true }
                                .testTag("language_selector")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    modifier = Modifier.size(15.dp),
                                    tint = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = selectedLanguage.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                                    maxLines = 1
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            AppLanguage.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${lang.displayName} (${lang.nativeName})")
                                    },
                                    onClick = {
                                        onLanguageSelected(lang)
                                        languageMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // User Profile Chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDarkMode) DarkBackground else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDarkMode) DarkCardBorder else CardBorderColor
                        ),
                        modifier = Modifier
                            .clickable { onDestinationSelected(NavDestination.PROFILE) }
                            .testTag("user_profile_chip")
                    ) {
                        Box(
                            modifier = Modifier.padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color(0xFF616161),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Green online indicator dot
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32))
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}
