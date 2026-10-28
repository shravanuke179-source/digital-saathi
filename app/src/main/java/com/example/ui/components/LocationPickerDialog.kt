package com.example.ui.components

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
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
import com.example.data.model.AppLanguage
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.theme.*
import com.example.util.LocationHelper
import com.example.util.UserLocationInfo

@Composable
fun LocationPickerDialog(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentLocation by repository.selectedLocation.collectAsState()
    val isGpsActive by repository.isGpsActive.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isDetectingGps by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "gps_detect_anim")
    val gpsRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gps_rotation"
    )

    fun startGpsDetection() {
        isDetectingGps = true
        LocationHelper.fetchCurrentLocation(
            context = context,
            onSuccess = { locationInfo ->
                isDetectingGps = false
                repository.applyLocation(
                    latitude = locationInfo.latitude,
                    longitude = locationInfo.longitude,
                    formattedName = locationInfo.formattedName,
                    stateName = locationInfo.state,
                    isLiveGps = true
                )
                Toast.makeText(
                    context,
                    "📍 Location updated: ${locationInfo.formattedName}",
                    Toast.LENGTH_SHORT
                ).show()
                onDismiss()
            },
            onError = { errMsg ->
                isDetectingGps = false
                Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
            }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            startGpsDetection()
        } else {
            Toast.makeText(
                context,
                "Location permission denied. You can still select your region manually below.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val filteredHubs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            LocationHelper.popularAgriHubs
        } else {
            LocationHelper.popularAgriHubs.filter { hub ->
                hub.locality.contains(searchQuery, ignoreCase = true) ||
                    hub.district.contains(searchQuery, ignoreCase = true) ||
                    hub.state.contains(searchQuery, ignoreCase = true) ||
                    hub.formattedName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> "Location & Mandi Region"
                                    AppLanguage.HINDI -> "स्थान एवं मंडी क्षेत्र"
                                    AppLanguage.MARATHI -> "स्थान व बाजार समिती"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )
                            Text(
                                text = "Current: $currentLocation",
                                fontSize = 11.sp,
                                color = if (isGpsActive) ForestGreen else IndigoSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GPS Auto Detection Action Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDarkMode) Color(0xFF1E281E) else Color(0xFFF1F8F1),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF334A33) else Color(0xFFC5E1A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> "Live Device GPS Detection"
                                    AppLanguage.HINDI -> "लाइव जीपीएस स्थान पहचान"
                                    AppLanguage.MARATHI -> "थेट जीपीएस स्थान शोध"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.ENGLISH -> "Fetches hyper-local Open-Meteo weather and APMC mandi commodity prices for your exact village."
                                AppLanguage.HINDI -> "आपके गांव के लिए सटीक मौसम एवं स्थानीय मंडी के ताज़ा थोक भाव प्राप्त करता है।"
                                AppLanguage.MARATHI -> "तुमच्या गावासाठी अचूक हवामान व स्थानिक बाजार समितीचे भाव मिळवते."
                            },
                            fontSize = 11.sp,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (LocationHelper.hasLocationPermission(context)) {
                                    startGpsDetection()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            enabled = !isDetectingGps,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("detect_gps_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDetectingGps) Icons.Default.Sync else Icons.Default.MyLocation,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .then(if (isDetectingGps) Modifier.rotate(gpsRotation) else Modifier)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isDetectingGps) "Detecting Location..." else "Allow Permission & Detect Live GPS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search field for manual selection
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search district, mandi or state...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IndigoSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Or Select Agricultural Hub / State Mandi:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                // List of popular Agricultural hubs
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredHubs) { hub ->
                        val isSelected = currentLocation.contains(hub.locality, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) (if (isDarkMode) Color(0xFF263326) else Color(0xFFE8F5E9)) else (if (isDarkMode) DarkBackground else Color(0xFFF9F7F2)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ForestGreen else (if (isDarkMode) DarkCardBorder else CardBorderColor)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repository.applyLocation(
                                        latitude = hub.latitude,
                                        longitude = hub.longitude,
                                        formattedName = hub.formattedName,
                                        stateName = hub.state,
                                        isLiveGps = false
                                    )
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = hub.formattedName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) ForestGreen else (if (isDarkMode) DarkTextPrimary else TextPrimaryDark)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreen
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${hub.state} • ${String.format(java.util.Locale.US, "%.2f°N, %.2f°E", hub.latitude, hub.longitude)}",
                                        fontSize = 10.sp,
                                        color = TextSecondaryMuted
                                    )
                                }

                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = if (isSelected) ForestGreen else TextSecondaryMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
