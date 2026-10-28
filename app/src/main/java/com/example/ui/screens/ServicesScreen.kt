package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.theme.*

enum class ServicesSubTab(
    val labelEn: String,
    val labelHi: String,
    val labelMr: String,
    val icon: ImageVector
) {
    ALL("All Services", "सभी सेवाएं", "सर्व सेवा", Icons.Default.Apps),
    PRODUCE("Sell Produce", "फसल बिक्री", "शेतमाल विक्री", Icons.Default.Storefront),
    EQUIPMENT("Equipment", "कृषि उपकरण", "शेती अवजारे", Icons.Default.Build),
    SCHEMES("Schemes", "योजनाएं", "योजना", Icons.Default.Description),
    KHETI("Kheti Hub", "खेती हब", "शेती हब", Icons.Default.Agriculture),
    SHIKSHA("Shiksha", "शिक्षा", "शिक्षण", Icons.Default.School),
    SOS("Village SOS", "ग्राम SOS", "गाव SOS", Icons.Default.Emergency)
}

@Composable
fun ServicesScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    initialTab: ServicesSubTab = ServicesSubTab.ALL,
    onSelectScheme: (Scheme) -> Unit = {},
    onVoiceSpeak: (String) -> Unit = {},
    onNavigateBackToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember(initialTab) { mutableStateOf(initialTab) }
    var activeServiceDialog by remember { mutableStateOf<CitizenService?>(null) }
    val services = repository.citizenServices

    // Handle back button press
    BackHandler {
        if (currentSubTab != ServicesSubTab.ALL) {
            currentSubTab = ServicesSubTab.ALL
        } else {
            onNavigateBackToHome()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
    ) {
        // Sub-Navigation Tabs Row
        Surface(
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isDarkMode) DarkCardBorder else CardBorderColor
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServicesSubTab.values().forEach { tab ->
                    val isSelected = currentSubTab == tab
                    val label = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> tab.labelEn
                        AppLanguage.HINDI -> tab.labelHi
                        AppLanguage.MARATHI -> tab.labelMr
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) {
                            if (isDarkMode) Color(0xFF2D3250) else ActiveNavPill
                        } else {
                            if (isDarkMode) DarkBackground else Color(0xFFF7F5F0)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) {
                                if (isDarkMode) Color(0xFF434E80) else Color(0xFFD6DBF8)
                            } else {
                                if (isDarkMode) DarkCardBorder else CardBorderColor
                            }
                        ),
                        modifier = Modifier
                            .clickable { currentSubTab = tab }
                            .testTag("services_subtab_${tab.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) {
                                    if (isDarkMode) IndigoContainer else ActiveNavText
                                } else {
                                    if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (isDarkMode) IndigoContainer else ActiveNavText
                                    } else {
                                        if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }

        // Tab Content
        when (currentSubTab) {
            ServicesSubTab.ALL -> {
                ServicesDirectoryContent(
                    services = services,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onOpenSubTab = { tab -> currentSubTab = tab },
                    onSelectService = { service -> activeServiceDialog = service }
                )
            }
            ServicesSubTab.PRODUCE -> {
                SellProduceScreen(
                    repository = repository,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    initialTab = SellProduceTab.MY_LISTINGS,
                    onNavigateBack = { currentSubTab = ServicesSubTab.ALL }
                )
            }
            ServicesSubTab.EQUIPMENT -> {
                FarmEquipmentScreen(
                    repository = repository,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    initialTab = EquipmentViewTab.BROWSE,
                    onNavigateBack = { currentSubTab = ServicesSubTab.ALL }
                )
            }
            ServicesSubTab.SCHEMES -> {
                SchemesScreen(
                    repository = repository,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onSelectScheme = onSelectScheme
                )
            }
            ServicesSubTab.KHETI -> {
                KhetiScreen(
                    repository = repository,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onNavigateToSchemes = { currentSubTab = ServicesSubTab.SCHEMES }
                )
            }
            ServicesSubTab.SHIKSHA -> {
                DigitalShikshaScreen(
                    repository = repository,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onVoiceSpeak = onVoiceSpeak
                )
            }
            ServicesSubTab.SOS -> {
                VillageSosScreen(
                    repository = repository,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode
                )
            }
        }
    }

    // Interactive Service Action Dialog
    activeServiceDialog?.let { service ->
        ServiceActionModal(
            service = service,
            userProfile = repository.userProfile.collectAsState().value,
            isDarkMode = isDarkMode,
            onDismiss = { activeServiceDialog = null }
        )
    }
}

@Composable
private fun ServicesDirectoryContent(
    services: List<CitizenService>,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onOpenSubTab: (ServicesSubTab) -> Unit,
    onSelectService: (CitizenService) -> Unit
) {
    val sections = remember(services) {
        services.groupBy { it.sectionEn }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Directory Header
        item {
            Column {
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Citizen Services & Hubs"
                        AppLanguage.HINDI -> "नागरिक सेवाएं एवं हब"
                        AppLanguage.MARATHI -> "नागरिक सेवा आणि हब"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Access government schemes, agriculture, education, SOS, and utility documents"
                        AppLanguage.HINDI -> "सरकारी योजनाएं, कृषि, शिक्षा, SOS और जनोपयोगी दस्तावेज"
                        AppLanguage.MARATHI -> "शासकीय योजना, शेती, शिक्षण, SOS आणि उपयुक्त दस्तऐवज"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                )
            }
        }

        // Featured Integrated Hubs (Schemes, Kheti, Shiksha, SOS)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Integrated Portals (मुख्य पोर्टल)"
                        AppLanguage.HINDI -> "मुख्य पोर्टल एवं सेवाएं"
                        AppLanguage.MARATHI -> "मुख्य पोर्टल आणि सेवा"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TerracottaPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HubShortcutCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Govt Schemes"
                            AppLanguage.HINDI -> "सरकारी योजनाएं"
                            AppLanguage.MARATHI -> "शासकीय योजना"
                        },
                        subtitle = "Subsidies & welfare",
                        icon = Icons.Default.Description,
                        iconTint = IndigoSecondary,
                        bgColor = if (isDarkMode) Color(0xFF282538) else Color(0xFFF3F4FD),
                        borderColor = if (isDarkMode) Color(0xFF3F466E) else Color(0xFFD6DBF8),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSubTab(ServicesSubTab.SCHEMES) }
                    )

                    HubShortcutCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Kheti Hub"
                            AppLanguage.HINDI -> "खेती व मंडी भाव"
                            AppLanguage.MARATHI -> "शेती व बाजार भाव"
                        },
                        subtitle = "Mandi rates & crops",
                        icon = Icons.Default.Agriculture,
                        iconTint = ForestGreen,
                        bgColor = if (isDarkMode) Color(0xFF222E22) else Color(0xFFF1F8F1),
                        borderColor = if (isDarkMode) Color(0xFF355235) else Color(0xFFCCE4CC),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSubTab(ServicesSubTab.KHETI) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HubShortcutCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Digital Shiksha"
                            AppLanguage.HINDI -> "डिजिटल साक्षरता"
                            AppLanguage.MARATHI -> "डिजिटल शिक्षण"
                        },
                        subtitle = "UPI & cyber safety",
                        icon = Icons.Default.School,
                        iconTint = AmberAccent,
                        bgColor = if (isDarkMode) Color(0xFF2E271F) else Color(0xFFFFF9F0),
                        borderColor = if (isDarkMode) Color(0xFF574630) else Color(0xFFFFE0B2),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSubTab(ServicesSubTab.SHIKSHA) }
                    )

                    HubShortcutCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Village SOS"
                            AppLanguage.HINDI -> "ग्राम पंचायत SOS"
                            AppLanguage.MARATHI -> "गाव SOS व सूचना"
                        },
                        subtitle = "Helpline & notices",
                        icon = Icons.Default.Emergency,
                        iconTint = AlertRed,
                        bgColor = if (isDarkMode) Color(0xFF312121) else Color(0xFFFFF2F2),
                        borderColor = if (isDarkMode) Color(0xFF5E3535) else Color(0xFFFFCDD2),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSubTab(ServicesSubTab.SOS) }
                    )
                }
            }
        }

        // Farmer Services Dedicated Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Farmer Services (किसान सेवाएं)"
                        AppLanguage.HINDI -> "किसान सेवाएं एवं उपकरण"
                        AppLanguage.MARATHI -> "शेतकरी सेवा व अवजारे"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ForestGreen
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HubShortcutCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Sell Farm Produce"
                            AppLanguage.HINDI -> "फसल बेचें (मंडी/क्रेता)"
                            AppLanguage.MARATHI -> "शेती माल विका"
                        },
                        subtitle = "Direct to mandi & buyers",
                        icon = Icons.Default.Storefront,
                        iconTint = ForestGreen,
                        bgColor = if (isDarkMode) Color(0xFF1E281E) else Color(0xFFF1F8F1),
                        borderColor = if (isDarkMode) Color(0xFF334A33) else Color(0xFFC5E1A5),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSubTab(ServicesSubTab.PRODUCE) }
                    )

                    HubShortcutCard(
                        title = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Farm Equipment"
                            AppLanguage.HINDI -> "कृषि उपकरण बुकिंग"
                            AppLanguage.MARATHI -> "शेती अवजारे बुकिंग"
                        },
                        subtitle = "Tractors & harvesters",
                        icon = Icons.Default.Build,
                        iconTint = IndigoSecondary,
                        bgColor = if (isDarkMode) Color(0xFF282538) else Color(0xFFF3F4FD),
                        borderColor = if (isDarkMode) Color(0xFF3F466E) else Color(0xFFD6DBF8),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSubTab(ServicesSubTab.EQUIPMENT) }
                    )
                }

                // Quick Farmer Action Pills (My Farm Sales, Delivery Tracking, Equipment Bookings)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDarkMode) Color(0xFF232B23) else Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen),
                        modifier = Modifier.clickable { onOpenSubTab(ServicesSubTab.PRODUCE) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📦", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("My Farm Sales", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDarkMode) Color(0xFF242834) else Color(0xFFEEF2FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IndigoSecondary),
                        modifier = Modifier.clickable { onOpenSubTab(ServicesSubTab.PRODUCE) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🚚", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delivery Tracking", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDarkMode) Color(0xFF2C241E) else Color(0xFFFFF3E0),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertOrange),
                        modifier = Modifier.clickable { onOpenSubTab(ServicesSubTab.EQUIPMENT) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔧", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("My Equipment Bookings", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertOrange)
                        }
                    }
                }
            }
        }

        // Categorized Document & Utility Services
        sections.forEach { (sectionTitle, serviceList) ->
            item {
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TerracottaPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(serviceList) { service ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectService(service) }
                        .testTag("service_card_${service.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (service.iconType) {
                                        "aadhaar", "pan", "ration" -> Color(0xFFE8EDFF)
                                        "electric", "water", "gas" -> Color(0xFFFFF3E0)
                                        else -> Color(0xFFE8F5E9)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (service.iconType) {
                                    "aadhaar" -> Icons.Default.Badge
                                    "pan" -> Icons.Default.CreditCard
                                    "ration" -> Icons.Default.ShoppingCart
                                    "electric" -> Icons.Default.Bolt
                                    "water" -> Icons.Default.WaterDrop
                                    "gas" -> Icons.Default.LocalFireDepartment
                                    "land" -> Icons.Default.Terrain
                                    else -> Icons.Default.Security
                                },
                                contentDescription = null,
                                tint = when (service.iconType) {
                                    "aadhaar", "pan", "ration" -> IndigoSecondary
                                    "electric", "water", "gas" -> AmberAccent
                                    else -> ForestGreen
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedLanguage == AppLanguage.HINDI) service.titleHi else service.titleEn,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (selectedLanguage == AppLanguage.HINDI) service.descriptionHi else service.descriptionEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) service.actionTextHi else service.actionTextEn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HubShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextSecondaryMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ServiceActionModal(
    service: CitizenService,
    userProfile: UserProfile,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
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
                        text = service.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (service.iconType) {
                    "aadhaar" -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9F9FB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0D7DE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("UNIQUE IDENTIFICATION AUTHORITY OF INDIA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFE8F5E9))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(if (userProfile.isVerified) "VERIFIED" else "NOT LINKED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0E0E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(userProfile.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Identity details require official verification", fontSize = 11.sp, color = TextSecondaryMuted)
                                        Text("${userProfile.village}, ${userProfile.district}", fontSize = 11.sp, color = TextSecondaryMuted)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "No Aadhaar document linked",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = TextPrimaryDark,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }

                    "electric" -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFDE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFF59D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Consumer account not linked", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Bill period: unavailable", fontSize = 12.sp)
                                Text("Current bill: unavailable", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBF360C))
                                Text("Connect through the official electricity provider", fontSize = 12.sp, color = ForestGreen)
                            }
                        }
                    }

                    "ration" -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F8E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5E1A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Ration Card: ${userProfile.rationCardNo}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Scheme: confirm through the official ration portal", fontSize = 12.sp)
                                Text("Monthly quota: unavailable", fontSize = 12.sp, color = ForestGreen, fontWeight = FontWeight.SemiBold)
                                Text("Assigned FPS shop: unavailable", fontSize = 12.sp)
                            }
                        }
                    }

                    "land" -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3E5F5),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1BEE7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Survey/Gat number: no land record linked", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Landholder: ${userProfile.name}", fontSize = 12.sp)
                                Text("Total area: unavailable", fontSize = 12.sp, color = IndigoSecondary, fontWeight = FontWeight.SemiBold)
                                Text("No certified digital copy linked", fontSize = 12.sp)
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = "Use the official provider for ${service.titleEn}. No provider account is linked.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Window")
                }
            }
        }
    }
}
