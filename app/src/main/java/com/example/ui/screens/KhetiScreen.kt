package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.components.LocationPickerDialog
import com.example.ui.theme.*
import com.example.util.LocationHelper

@Composable
fun KhetiScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onNavigateToSchemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mandiPrices by repository.mandiPrices.collectAsState()
    val weatherData by repository.weatherData.collectAsState()
    val isWeatherLoading by repository.isWeatherLoading.collectAsState()
    val isMandiLoading by repository.isMandiLoading.collectAsState()
    val isRefreshing = isWeatherLoading || isMandiLoading

    val selectedLocation by repository.selectedLocation.collectAsState()
    val detectedState by repository.detectedState.collectAsState()
    val isGpsActive by repository.isGpsActive.collectAsState()
    val userCoordinates by repository.userCoordinates.collectAsState()

    var showLocationDialog by remember { mutableStateOf(false) }
    var isDetectingGpsLocally by remember { mutableStateOf(false) }

    fun triggerGpsLocally() {
        isDetectingGpsLocally = true
        LocationHelper.fetchCurrentLocation(
            context = context,
            onSuccess = { info ->
                isDetectingGpsLocally = false
                repository.applyLocation(
                    latitude = info.latitude,
                    longitude = info.longitude,
                    formattedName = info.formattedName,
                    stateName = info.state,
                    isLiveGps = true
                )
                Toast.makeText(context, "📍 Live Weather & Mandi updated for ${info.formattedName}", Toast.LENGTH_SHORT).show()
            },
            onError = { err ->
                isDetectingGpsLocally = false
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            triggerGpsLocally()
        } else {
            Toast.makeText(
                context,
                "Location permission denied. Tap 'Change Region' to pick your location manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val cropAdvisories = repository.cropAdvisories
    var selectedCropDetail by remember { mutableStateOf<CropAdvisory?>(null) }
    var mandiSearchQuery by remember { mutableStateOf("") }
    var selectedStateFilter by remember { mutableStateOf("All") }

    val statesList = listOf("All", "Maharashtra", "Madhya Pradesh", "Rajasthan", "Punjab", "Himachal Pradesh", "Gujarat")

    val filteredMandi = remember(mandiPrices, mandiSearchQuery, selectedStateFilter) {
        mandiPrices.filter { item ->
            val matchesSearch = mandiSearchQuery.isBlank() ||
                item.commodityEn.contains(mandiSearchQuery, ignoreCase = true) ||
                item.commodityHi.contains(mandiSearchQuery, ignoreCase = true) ||
                item.commodityMr.contains(mandiSearchQuery, ignoreCase = true) ||
                item.mandiEn.contains(mandiSearchQuery, ignoreCase = true) ||
                item.mandiHi.contains(mandiSearchQuery, ignoreCase = true)

            val matchesState = selectedStateFilter == "All" || item.stateEn.equals(selectedStateFilter, ignoreCase = true)
            matchesSearch && matchesState
        }
    }

    // Refresh rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_anim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header with Location Selector & Real-Time Refresh Action
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.ENGLISH -> "Kheti & Mandi Hub"
                                AppLanguage.HINDI -> "खेती एवं मंडी भाव हब"
                                AppLanguage.MARATHI -> "शेती व बाजार भाव हब"
                            },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.ENGLISH -> "Live Open-Meteo weather & real-time APMC mandi rates"
                                AppLanguage.HINDI -> "ताज़ा मौसम पूर्वानुमान एवं वास्तविक समय मंडी भाव"
                                AppLanguage.MARATHI -> "थेट हवामान अंदाज आणि थेट बाजार समिती भाव"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )
                    }

                    // Refresh Button
                    Button(
                        onClick = { repository.refreshAllRealTimeData() },
                        enabled = !isRefreshing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xFF2D3250) else Color(0xFFE8F5E9)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("kheti_refresh_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = ForestGreen,
                                modifier = Modifier
                                    .size(16.dp)
                                    .then(if (isRefreshing) Modifier.rotate(rotationAngle) else Modifier)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRefreshing) "Syncing..." else "Refresh",
                                color = ForestGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location & GPS Permission Action Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) Color(0xFF242C24) else Color(0xFFEAF5EC),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDarkMode) Color(0xFF384F38) else Color(0xFFC3E5C8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isGpsActive) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedLocation,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                    )
                                    if (isGpsActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ForestGreen)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("LIVE GPS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                                Text(
                                    text = "Coordinates: ${String.format(java.util.Locale.US, "%.2f°N, %.2f°E", userCoordinates.first, userCoordinates.second)}",
                                    fontSize = 10.sp,
                                    color = TextSecondaryMuted
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // GPS Detect 1-tap Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ForestGreen,
                                modifier = Modifier
                                    .clickable {
                                        if (LocationHelper.hasLocationPermission(context)) {
                                            triggerGpsLocally()
                                        } else {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    }
                                    .testTag("detect_gps_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isDetectingGpsLocally) "Locating..." else "Use GPS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Change Region Picker Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDarkMode) Color(0xFF333B33) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen),
                                modifier = Modifier
                                    .clickable { showLocationDialog = true }
                                    .testTag("change_region_button")
                            ) {
                                Text(
                                    text = "Change",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Real-Time Weather Card (Powered by Open-Meteo API)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) Color(0xFF1E281E) else Color(0xFFF1F8F1),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDarkMode) Color(0xFF334A33) else Color(0xFFC5E1A5)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("realtime_weather_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row: Live indicator + timestamp
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
                                text = "LIVE WEATHER (OPEN-METEO API)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = weatherData.lastUpdatedTime,
                            fontSize = 10.sp,
                            color = TextSecondaryMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Main weather stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = weatherData.iconEmoji,
                                fontSize = 36.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", weatherData.temperature)}°C",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                )
                                Text(
                                    text = when (selectedLanguage) {
                                        AppLanguage.ENGLISH -> weatherData.conditionEn
                                        AppLanguage.HINDI -> weatherData.conditionHi
                                        AppLanguage.MARATHI -> weatherData.conditionMr
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForestGreen
                                )
                            }
                        }

                        // Mini metrics grid
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Feels like: ${String.format(java.util.Locale.US, "%.1f", weatherData.feelsLike)}°C",
                                fontSize = 11.sp,
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            )
                            Text(
                                text = "Humidity: ${weatherData.humidity}%",
                                fontSize = 11.sp,
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            )
                            Text(
                                text = "Wind: ${weatherData.windSpeed} km/h",
                                fontSize = 11.sp,
                                color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            )
                            Text(
                                text = "Rain Chance: ${weatherData.rainProbability}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (weatherData.rainProbability > 40) AlertRed else IndigoSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Farmer Advisory based on real-time weather
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkMode) Color(0xFF161F16) else Color.White.copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDarkMode) Color(0xFF2C3E2C) else Color(0xFFDCEDC8)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("🌾", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (selectedLanguage) {
                                        AppLanguage.ENGLISH -> "Agricultural Guidance (मौसम आधारित कृषि सलाह):"
                                        AppLanguage.HINDI -> "मौसम आधारित कृषि सलाह:"
                                        AppLanguage.MARATHI -> "हवामानावर आधारित कृषी सल्ला:"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (selectedLanguage) {
                                        AppLanguage.ENGLISH -> weatherData.farmingAdviceEn
                                        AppLanguage.HINDI -> weatherData.farmingAdviceHi
                                        AppLanguage.MARATHI -> weatherData.farmingAdviceMr
                                    },
                                    fontSize = 11.sp,
                                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // PMKSY Irrigation Scheme Promo Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) Color(0xFF282538) else Color(0xFFF3F4FD),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF3F466E) else Color(0xFFD6DBF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = IndigoSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pradhan Mantri Krishi Sinchayee Yojana (PMKSY)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = IndigoSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Apply for drip & sprinkler micro-irrigation systems with up to 55% government subsidy for village farmers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onNavigateToSchemes,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Apply for Irrigation Subsidy →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Mandi Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Live APMC Mandi Wholesale Rates"
                            AppLanguage.HINDI -> "दैनिक थोक मंडी भाव (APMC)"
                            AppLanguage.MARATHI -> "थेट कृषी उत्पन्न बाजार समिती भाव"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )
                    Text(
                        text = "${filteredMandi.size} commodities trading live across Indian mandis",
                        fontSize = 11.sp,
                        color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) Color(0xFF263326) else Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ForestGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }
                }
            }
        }

        // Mandi Search Bar
        item {
            OutlinedTextField(
                value = mandiSearchQuery,
                onValueChange = { mandiSearchQuery = it },
                placeholder = { Text("Search crop (गेहूं, soybean, cotton) or APMC...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (mandiSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { mandiSearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // State Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statesList) { state ->
                    val isSelected = selectedStateFilter == state
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            if (isDarkMode) Color(0xFF2D3250) else ActiveNavPill
                        } else {
                            if (isDarkMode) DarkCardSurface else Color.White
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) {
                                if (isDarkMode) Color(0xFF434E80) else Color(0xFFD6DBF8)
                            } else {
                                if (isDarkMode) DarkCardBorder else CardBorderColor
                            }
                        ),
                        modifier = Modifier.clickable { selectedStateFilter = state }
                    ) {
                        Text(
                            text = state,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                if (isDarkMode) IndigoContainer else ActiveNavText
                            } else {
                                if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Mandi Rate Cards with Live Rates, Trends and Arrivals
        items(filteredMandi) { item ->
            val commodityTitle = when (selectedLanguage) {
                AppLanguage.ENGLISH -> item.commodityEn
                AppLanguage.HINDI -> item.commodityHi
                AppLanguage.MARATHI -> item.commodityMr
            }
            val mandiTitle = when (selectedLanguage) {
                AppLanguage.ENGLISH -> item.mandiEn
                AppLanguage.HINDI -> item.mandiHi
                AppLanguage.MARATHI -> item.mandiMr
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mandi_card_${item.id}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = commodityTitle,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mandiTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Range: ₹${item.minPrice} - ₹${item.maxPrice}",
                                fontSize = 11.sp,
                                color = TextSubtle
                            )
                            Text(
                                text = "• Arrivals: ${item.arrivalQuantity}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = ForestGreen
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${item.modalPrice}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TerracottaPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "/ ${item.unit}",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val isUp = item.dailyTrend >= 0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isUp) "▲ +₹${item.dailyTrend}" else "▼ ₹${item.dailyTrend}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUp) ForestGreen else AlertRed
                            )
                        }
                    }
                }
            }
        }

        // Scientific Crop Advisories Section
        item {
            Text(
                text = when (selectedLanguage) {
                    AppLanguage.ENGLISH -> "Scientific Crop Cultivation Guides"
                    AppLanguage.HINDI -> "वैज्ञानिक फसल उत्पादन मार्गदर्शिका"
                    AppLanguage.MARATHI -> "वैज्ञानिक पीक उत्पादन मार्गदर्शक"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(cropAdvisories) { crop ->
            val cropName = when (selectedLanguage) {
                AppLanguage.ENGLISH -> crop.nameEn
                AppLanguage.HINDI -> crop.nameHi
                AppLanguage.MARATHI -> crop.nameMr
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedCropDetail = crop }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF263326) else Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cropName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )
                        Text(
                            text = "Season: ${crop.seasonEn} • Soil: ${crop.soilRequirementsEn}",
                            fontSize = 11.sp,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = TextSecondaryMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Crop Detail Dialog
    selectedCropDetail?.let { crop ->
        val cropName = when (selectedLanguage) {
            AppLanguage.ENGLISH -> crop.nameEn
            AppLanguage.HINDI -> crop.nameHi
            AppLanguage.MARATHI -> crop.nameMr
        }

        Dialog(onDismissRequest = { selectedCropDetail = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cropName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )
                        IconButton(onClick = { selectedCropDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🌱 Season: ${crop.seasonEn}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("🪴 Soil: ${crop.soilRequirementsEn}", fontSize = 12.sp)
                        Text("💧 Water: ${crop.waterRequirementsEn}", fontSize = 12.sp)
                        Text("📖 Guidance: ${crop.basicGuidanceEn}", fontSize = 12.sp)
                        Text("🐛 Common Pests: ${crop.commonPestsEn}", fontSize = 12.sp, color = AlertRed)
                        Text("🛡️ Prevention: ${crop.preventionEn}", fontSize = 12.sp, color = ForestGreen)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { selectedCropDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Advisory")
                    }
                }
            }
        }
    }

    // Location & GPS Mandi Picker Dialog
    if (showLocationDialog) {
        LocationPickerDialog(
            repository = repository,
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onDismiss = { showLocationDialog = false }
        )
    }
}
