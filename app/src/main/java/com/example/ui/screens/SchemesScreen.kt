package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.theme.*

@Composable
fun SchemesScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onSelectScheme: (Scheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val schemes by repository.schemes.collectAsState()
    var selectedCategory by remember { mutableStateOf(SchemeCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSchemes = remember(schemes, selectedCategory, searchQuery) {
        schemes.filter { scheme ->
            val matchesCategory = (selectedCategory == SchemeCategory.ALL) || (scheme.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                scheme.titleEn.contains(searchQuery, ignoreCase = true) ||
                scheme.titleHi.contains(searchQuery, ignoreCase = true) ||
                scheme.descriptionEn.contains(searchQuery, ignoreCase = true) ||
                scheme.benefitEn.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Header
        item {
            Column {
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Government Schemes Library"
                        AppLanguage.HINDI -> "सरकारी योजनाएं पुस्तकालय"
                        AppLanguage.MARATHI -> "शासकीय योजना संग्रह"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Find financial assistance, subsidies, pensions and scholarship schemes"
                        AppLanguage.HINDI -> "आर्थिक सहायता, सब्सिडी, पेंशन और छात्रवृत्ति योजनाओं की जानकारी"
                        AppLanguage.MARATHI -> "आर्थिक मदत, अनुदान, पेन्शन आणि शिष्यवृत्ती योजनांची माहिती"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                )
            }
        }

        // Search in schemes
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Filter schemes by name or benefit..."
                            AppLanguage.HINDI -> "योजना का नाम या लाभ खोजें..."
                            AppLanguage.MARATHI -> "योजनेचे नाव किंवा लाभ शोधा..."
                        },
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TerracottaPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TerracottaPrimary,
                    unfocusedBorderColor = if (isDarkMode) DarkCardBorder else CardBorderColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scheme_search_input")
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SchemeCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val label = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> cat.labelEn
                        AppLanguage.HINDI -> cat.labelHi
                        AppLanguage.MARATHI -> cat.labelMr
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDarkMode) IndigoSecondary else ActiveNavPill,
                            selectedLabelColor = if (isDarkMode) Color.White else ActiveNavText,
                            containerColor = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                            labelColor = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) IndigoSecondary else (if (isDarkMode) DarkCardBorder else CardBorderColor)
                        )
                    )
                }
            }
        }

        // Scheme Count
        item {
            Text(
                text = "${filteredSchemes.size} Schemes Available",
                style = MaterialTheme.typography.labelLarge,
                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
            )
        }

        // Scheme Cards
        items(filteredSchemes) { scheme ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scheme_card_${scheme.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top row: Category tag & Active badge & Bookmark
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("● Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8EDFF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(scheme.category.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                            }
                        }

                        IconButton(
                            onClick = { repository.toggleSchemeBookmark(scheme.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (scheme.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (scheme.isBookmarked) TerracottaPrimary else (if (isDarkMode) DarkTextSecondary else TextSubtle),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = if (selectedLanguage == AppLanguage.HINDI) scheme.titleHi else scheme.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Description
                    Text(
                        text = if (selectedLanguage == AppLanguage.HINDI) scheme.descriptionHi else scheme.descriptionEn,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Benefit Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkMode) Color(0xFF2E271F) else Color(0xFFFFF8E1),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF574630) else Color(0xFFFFECB3)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedLanguage == AppLanguage.HINDI) scheme.benefitHi else scheme.benefitEn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) AmberContainer else Color(0xFF5D4037)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Actions row: View Details & Apply Now
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSelectScheme(scheme) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onSelectScheme(scheme) },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("Apply Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
