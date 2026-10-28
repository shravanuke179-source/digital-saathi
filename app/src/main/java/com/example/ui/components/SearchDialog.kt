package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.theme.*

@Composable
fun GlobalSearchDialog(
    initialQuery: String = "",
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onNavigateToDestination: (NavDestination) -> Unit
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }

    val schemes by repository.schemes.collectAsState()
    val mandiPrices by repository.mandiPrices.collectAsState()
    val notices by repository.notices.collectAsState()
    val produceListings by repository.produceListings.collectAsState()
    val equipmentList by repository.farmEquipmentList.collectAsState()

    val filteredSchemes = remember(searchQuery, schemes) {
        if (searchQuery.isBlank()) emptyList()
        else schemes.filter {
            it.titleEn.contains(searchQuery, ignoreCase = true) ||
            it.titleHi.contains(searchQuery, ignoreCase = true) ||
            it.descriptionEn.contains(searchQuery, ignoreCase = true) ||
            it.benefitEn.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredProduce = remember(searchQuery, produceListings) {
        if (searchQuery.isBlank()) emptyList()
        else produceListings.filter {
            it.cropName.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.preferredBuyerOrMandi.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredEquipment = remember(searchQuery, equipmentList) {
        if (searchQuery.isBlank()) emptyList()
        else equipmentList.filter {
            it.nameEn.contains(searchQuery, ignoreCase = true) ||
            it.nameHi.contains(searchQuery, ignoreCase = true) ||
            it.providerName.contains(searchQuery, ignoreCase = true) ||
            it.category.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredServices = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else repository.citizenServices.filter {
            it.titleEn.contains(searchQuery, ignoreCase = true) ||
            it.titleHi.contains(searchQuery, ignoreCase = true) ||
            it.descriptionEn.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredMandi = remember(searchQuery, mandiPrices) {
        if (searchQuery.isBlank()) emptyList()
        else mandiPrices.filter {
            it.commodityEn.contains(searchQuery, ignoreCase = true) ||
            it.commodityHi.contains(searchQuery, ignoreCase = true) ||
            it.mandiEn.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredLessons = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else repository.digitalLessons.filter {
            it.titleEn.contains(searchQuery, ignoreCase = true) ||
            it.titleHi.contains(searchQuery, ignoreCase = true) ||
            it.descriptionEn.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredNotices = remember(searchQuery, notices) {
        if (searchQuery.isBlank()) emptyList()
        else notices.filter {
            it.titleEn.contains(searchQuery, ignoreCase = true) ||
            it.titleHi.contains(searchQuery, ignoreCase = true) ||
            it.descriptionEn.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Search Digital Saathi"
                            AppLanguage.HINDI -> "डिजिटल साथी में खोजें"
                            AppLanguage.MARATHI -> "डिजिटल साथी मध्ये शोधा"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.ENGLISH -> "Search schemes, crops, services, upi..."
                                AppLanguage.HINDI -> "योजनाएं, फसल, सेवाएं, यूपीआई खोजें..."
                                AppLanguage.MARATHI -> "योजना, पिके, सेवा, यूपीआय शोधा..."
                            },
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TerracottaPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = if (isDarkMode) DarkCardBorder else CardBorderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (searchQuery.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = if (isDarkMode) DarkTextSecondary else TextSubtle
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> "Type keywords like 'tractor', 'wheat', 'upi', 'ration', 'health'"
                                    AppLanguage.HINDI -> "कीवर्ड टाइप करें जैसे 'ट्रैक्टर', 'गेहूं', 'यूपीआई', 'राशन'"
                                    AppLanguage.MARATHI -> "कीवर्ड टाईप करा जसे 'ट्रॅक्टर', 'गहू', 'यूपीआय', 'रेशन'"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Schemes Results
                        if (filteredSchemes.isNotEmpty()) {
                            item {
                                SearchCategoryHeader("🏛️ Government Schemes (${filteredSchemes.size})", isDarkMode)
                            }
                            items(filteredSchemes) { scheme ->
                                SearchResultCard(
                                    title = if (selectedLanguage == AppLanguage.HINDI) scheme.titleHi else scheme.titleEn,
                                    subtitle = if (selectedLanguage == AppLanguage.HINDI) scheme.benefitHi else scheme.benefitEn,
                                    badge = "Scheme",
                                    badgeColor = IndigoSecondary,
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        onDismiss()
                                        onNavigateToDestination(NavDestination.SCHEMES)
                                    }
                                )
                            }
                        }

                        // Farmer Produce Results
                        if (filteredProduce.isNotEmpty()) {
                            item {
                                SearchCategoryHeader("🌾 Farm Produce Listings (${filteredProduce.size})", isDarkMode)
                            }
                            items(filteredProduce) { produce ->
                                SearchResultCard(
                                    title = "${produce.cropName} (${produce.quantity} ${produce.unit})",
                                    subtitle = "Expected: ₹${produce.expectedPricePerUnit.toInt()}/Qtl • ${produce.pickupLocation}",
                                    badge = "Sell Produce",
                                    badgeColor = ForestGreen,
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        onDismiss()
                                        onNavigateToDestination(NavDestination.SELL_PRODUCE)
                                    }
                                )
                            }
                        }

                        // Farm Machinery Results
                        if (filteredEquipment.isNotEmpty()) {
                            item {
                                SearchCategoryHeader("🚜 Farm Machinery (${filteredEquipment.size})", isDarkMode)
                            }
                            items(filteredEquipment) { eq ->
                                SearchResultCard(
                                    title = if (selectedLanguage == AppLanguage.HINDI) eq.nameHi else eq.nameEn,
                                    subtitle = "₹${eq.priceAmount}/${eq.pricingUnit} • ${eq.providerName}",
                                    badge = "Equipment",
                                    badgeColor = IndigoSecondary,
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        onDismiss()
                                        onNavigateToDestination(NavDestination.FARM_EQUIPMENT)
                                    }
                                )
                            }
                        }

                        // Services Results
                        if (filteredServices.isNotEmpty()) {
                            item {
                                SearchCategoryHeader("🪪 Citizen Services (${filteredServices.size})", isDarkMode)
                            }
                            items(filteredServices) { service ->
                                SearchResultCard(
                                    title = if (selectedLanguage == AppLanguage.HINDI) service.titleHi else service.titleEn,
                                    subtitle = if (selectedLanguage == AppLanguage.HINDI) service.descriptionHi else service.descriptionEn,
                                    badge = "Service",
                                    badgeColor = ForestGreen,
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        onDismiss()
                                        onNavigateToDestination(NavDestination.SERVICES)
                                    }
                                )
                            }
                        }

                        // Mandi Crop Results
                        if (filteredMandi.isNotEmpty()) {
                            item {
                                SearchCategoryHeader("🌾 Mandi Rates (${filteredMandi.size})", isDarkMode)
                            }
                            items(filteredMandi) { mandi ->
                                SearchResultCard(
                                    title = "${mandi.commodityEn} - ₹${mandi.modalPrice}/${mandi.unit}",
                                    subtitle = mandi.mandiEn,
                                    badge = "Mandi",
                                    badgeColor = AmberAccent,
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        onDismiss()
                                        onNavigateToDestination(NavDestination.KHETI)
                                    }
                                )
                            }
                        }

                        // Lessons
                        if (filteredLessons.isNotEmpty()) {
                            item {
                                SearchCategoryHeader("📱 Digital Shiksha (${filteredLessons.size})", isDarkMode)
                            }
                            items(filteredLessons) { lesson ->
                                SearchResultCard(
                                    title = if (selectedLanguage == AppLanguage.HINDI) lesson.titleHi else lesson.titleEn,
                                    subtitle = if (selectedLanguage == AppLanguage.HINDI) lesson.descriptionHi else lesson.descriptionEn,
                                    badge = "Lesson",
                                    badgeColor = TerracottaPrimary,
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        onDismiss()
                                        onNavigateToDestination(NavDestination.SHIKSHA)
                                    }
                                )
                            }
                        }

                        // Notices
                        if (filteredNotices.isNotEmpty()) {
                            item {
                                SearchCategoryHeader("📢 Village Notices (${filteredNotices.size})", isDarkMode)
                            }
                            items(filteredNotices) { notice ->
                                SearchResultCard(
                                    title = if (selectedLanguage == AppLanguage.HINDI) notice.titleHi else notice.titleEn,
                                    subtitle = if (selectedLanguage == AppLanguage.HINDI) notice.descriptionHi else notice.descriptionEn,
                                    badge = "Notice",
                                    badgeColor = Color(0xFF5C6BC0),
                                    isDarkMode = isDarkMode,
                                    onClick = {
                                        onDismiss()
                                        onNavigateToDestination(NavDestination.VILLAGE_SOS)
                                    }
                                )
                            }
                        }

                        if (filteredSchemes.isEmpty() && filteredServices.isEmpty() && filteredMandi.isEmpty() && filteredLessons.isEmpty() && filteredNotices.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No matching records found for '$searchQuery'",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCategoryHeader(title: String, isDarkMode: Boolean) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchResultCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDarkMode) DarkBackground else WarmCreamBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isDarkMode) DarkTextSecondary else TextSubtle
            )
        }
    }
}
