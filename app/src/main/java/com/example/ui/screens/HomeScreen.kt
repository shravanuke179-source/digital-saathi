package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.components.LocationPickerDialog
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onNavigateTo: (NavDestination) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSchemeDetail: (Scheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val selectedLocation by repository.selectedLocation.collectAsState()
    val isGpsActive by repository.isGpsActive.collectAsState()
    val schemes by repository.schemes.collectAsState()
    val notices by repository.notices.collectAsState()
    val weatherData by repository.weatherData.collectAsState()
    val mandiPrices by repository.mandiPrices.collectAsState()

    var showLocationPicker by remember { mutableStateOf(false) }

    val greetingText = when (selectedLanguage) {
        AppLanguage.ENGLISH -> "Namaste, ${userProfile.name}!"
        AppLanguage.HINDI -> "नमस्ते, ${userProfile.name}!"
        AppLanguage.MARATHI -> "नमस्कार, ${userProfile.name}!"
    }

    val subtitleText = when (selectedLanguage) {
        AppLanguage.ENGLISH -> "Welcome to your digital companion for government, farming & village services"
        AppLanguage.HINDI -> "सरकारी योजनाओं, खेती और ग्रामीण सेवाओं के लिए आपका डिजिटल साथी"
        AppLanguage.MARATHI -> "शासकीय योजना, शेती आणि ग्रामीण सेवांसाठी तुमचा डिजिटल साथी"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Location Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_banner_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greetingText,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = if (isDarkMode) TerracottaContainer else TerracottaPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            )
                        }

                        // Live Real-Time Weather mini badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) Color(0xFF2D3250) else Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF3F51B5) else Color(0xFFC8E6C9)),
                            modifier = Modifier
                                .clickable { onNavigateTo(NavDestination.KHETI) }
                                .testTag("home_weather_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${weatherData.iconEmoji} ${String.format(java.util.Locale.US, "%.0f", weatherData.temperature)}°C",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location bar with Live Weather Condition
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDarkMode) DarkBackground else Color(0xFFFAF7F2))
                            .clickable { showLocationPicker = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("home_location_row")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isGpsActive) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isGpsActive) ForestGreen else TerracottaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedLocation,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )
                            if (isGpsActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• GPS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> weatherData.conditionEn
                                    AppLanguage.HINDI -> weatherData.conditionHi
                                    AppLanguage.MARATHI -> weatherData.conditionMr
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.EditLocationAlt,
                                contentDescription = "Change Location",
                                tint = TextSecondaryMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Global Search Trigger
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSearch)
                    .testTag("home_search_trigger")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Search schemes, services, crops, mandi prices..."
                            AppLanguage.HINDI -> "योजनाएं, सेवाएं, फसल, मंडी भाव खोजें..."
                            AppLanguage.MARATHI -> "योजना, सेवा, पिके, बाजार भाव शोधा..."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = if (isDarkMode) DarkTextSecondary else TextSubtle
                    )
                }
            }
        }

        // 4 Core Hero Cards
        item {
            Text(
                text = when (selectedLanguage) {
                    AppLanguage.ENGLISH -> "Quick Citizen Access (त्वरित सेवाएं)"
                    AppLanguage.HINDI -> "त्वरित नागरिक सेवाएं"
                    AppLanguage.MARATHI -> "त्वरित नागरिक सेवा"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Government Schemes"
                            AppLanguage.HINDI -> "सरकारी योजनाएं"
                            AppLanguage.MARATHI -> "शासकीय योजना"
                        },
                        subtitle = "Tractor subsidy, PM-Kisan & pensions",
                        icon = Icons.Default.Description,
                        iconTint = IndigoSecondary,
                        bgColor = if (isDarkMode) Color(0xFF282538) else Color(0xFFF3F4FD),
                        borderColor = if (isDarkMode) Color(0xFF3F466E) else Color(0xFFD6DBF8),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(NavDestination.SCHEMES) }
                    )

                    HeroCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Kheti & Mandi Hub"
                            AppLanguage.HINDI -> "खेती व मंडी भाव"
                            AppLanguage.MARATHI -> "शेती व बाजार भाव"
                        },
                        subtitle = "Live APMC rates & crop advisory",
                        icon = Icons.Default.Agriculture,
                        iconTint = ForestGreen,
                        bgColor = if (isDarkMode) Color(0xFF222E22) else Color(0xFFF1F8F1),
                        borderColor = if (isDarkMode) Color(0xFF355235) else Color(0xFFCCE4CC),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(NavDestination.KHETI) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Digital Shiksha"
                            AppLanguage.HINDI -> "डिजिटल साक्षरता"
                            AppLanguage.MARATHI -> "डिजिटल शिक्षण"
                        },
                        subtitle = "UPI simulator & cyber fraud rules",
                        icon = Icons.Default.School,
                        iconTint = AmberAccent,
                        bgColor = if (isDarkMode) Color(0xFF2E271F) else Color(0xFFFFF9F0),
                        borderColor = if (isDarkMode) Color(0xFF574630) else Color(0xFFFFE0B2),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(NavDestination.SHIKSHA) }
                    )

                    HeroCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Village SOS & Notice"
                            AppLanguage.HINDI -> "ग्राम पंचायत व SOS"
                            AppLanguage.MARATHI -> "गाव SOS व सूचना"
                        },
                        subtitle = "Emergency lines & panchayat notices",
                        icon = Icons.Default.Emergency,
                        iconTint = AlertRed,
                        bgColor = if (isDarkMode) Color(0xFF312121) else Color(0xFFFFF2F2),
                        borderColor = if (isDarkMode) Color(0xFF5E3535) else Color(0xFFFFCDD2),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(NavDestination.VILLAGE_SOS) }
                    )
                }
            }
        }

        // Farmer Services Section (Sell Farm Produce & Farm Equipment)
        item {
            Text(
                text = when (selectedLanguage) {
                    AppLanguage.ENGLISH -> "Farmer Services (किसान सेवाएं)"
                    AppLanguage.HINDI -> "किसान सेवाएं"
                    AppLanguage.MARATHI -> "शेतकरी सेवा"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeroCard(
                    title = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Sell Farm Produce"
                        AppLanguage.HINDI -> "फसल बेचें (मंडी/क्रेता)"
                        AppLanguage.MARATHI -> "शेती माल विका"
                    },
                    subtitle = "Sell directly to mandi or buyers",
                    icon = Icons.Default.Storefront,
                    iconTint = ForestGreen,
                    bgColor = if (isDarkMode) Color(0xFF1E281E) else Color(0xFFF1F8F1),
                    borderColor = if (isDarkMode) Color(0xFF334A33) else Color(0xFFC5E1A5),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(NavDestination.SELL_PRODUCE) }
                )

                HeroCard(
                    title = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Farm Equipment"
                        AppLanguage.HINDI -> "कृषि उपकरण बुकिंग"
                        AppLanguage.MARATHI -> "शेती अवजारे बुकिंग"
                    },
                    subtitle = "Book tractors, harvesters & machinery",
                    icon = Icons.Default.Build,
                    iconTint = IndigoSecondary,
                    bgColor = if (isDarkMode) Color(0xFF282538) else Color(0xFFF3F4FD),
                    borderColor = if (isDarkMode) Color(0xFF3F466E) else Color(0xFFD6DBF8),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(NavDestination.FARM_EQUIPMENT) }
                )
            }
        }

        // Announcement Banner: PM Kisan 17th Installment
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) Color(0xFF242F24) else Color(0xFFEFF8F0),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF3B5E3C) else Color(0xFFC7E6CA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ForestGreen)
                        )
                        Text(
                            text = "LATEST ANNOUNCEMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "PM-Kisan 17th Installment Released",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "₹2,000 credited to eligible bank accounts. Check your beneficiary e-KYC status and Aadhaar linkage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .clickable { onNavigateTo(NavDestination.SCHEMES) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View Scheme Details & Apply →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                }
            }
        }

        // Live APMC Mandi Rates Quick Ticker
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(NavDestination.KHETI) }
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
                                    .background(ForestGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> "Live Mandi Rates (ताज़ा थोक भाव)"
                                    AppLanguage.HINDI -> "ताज़ा दैनिक थोक मंडी भाव"
                                    AppLanguage.MARATHI -> "थेट दैनिक बाजार भाव"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )
                        }

                        Text(
                            text = "View All →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mandiPrices.take(5)) { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkMode) Color(0xFF1E261E) else Color(0xFFF4FAF4),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF2E3E2E) else Color(0xFFD4EBD4))
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                    Text(
                                        text = item.commodityEn.substringBefore(" ("),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${item.modalPrice} / ${item.unit}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TerracottaPrimary
                                    )
                                    Text(
                                        text = if (item.dailyTrend >= 0) "▲ +₹${item.dailyTrend}" else "▼ ₹${item.dailyTrend}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.dailyTrend >= 0) ForestGreen else AlertRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Popular Schemes Carousel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Popular Government Schemes"
                        AppLanguage.HINDI -> "लोकप्रिय सरकारी योजनाएं"
                        AppLanguage.MARATHI -> "लोकप्रिय शासकीय योजना"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                )

                TextButton(onClick = { onNavigateTo(NavDestination.SCHEMES) }) {
                    Text("View All", fontSize = 12.sp, color = IndigoSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schemes.take(4)) { scheme ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                        modifier = Modifier
                            .width(260.dp)
                            .clickable { onOpenSchemeDetail(scheme) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (selectedLanguage == AppLanguage.HINDI) scheme.titleHi else scheme.titleEn,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                                maxLines = 2,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (selectedLanguage == AppLanguage.HINDI) scheme.benefitHi else scheme.benefitEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = TerracottaPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Details →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationPicker) {
        LocationPickerDialog(
            repository = repository,
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onDismiss = { showLocationPicker = false }
        )
    }
}

@Composable
private fun HeroCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 13.sp,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = TextSecondaryMuted,
                    maxLines = 2
                )
            }
        }
    }
}
