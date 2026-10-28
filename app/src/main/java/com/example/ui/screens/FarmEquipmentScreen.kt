package com.example.ui.screens

import android.widget.Toast
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
import com.example.ui.components.rememberCloudAction
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
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.theme.*

enum class EquipmentViewTab(
    val labelEn: String,
    val labelHi: String,
    val labelMr: String
) {
    BROWSE("Browse Machinery", "कृषि मशीनें देखें", "शेती अवजारे शोधा"),
    MY_BOOKINGS("My Bookings", "मेरी बुकिंग", "माझी बुकिंग")
}

@Composable
fun FarmEquipmentScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    initialTab: EquipmentViewTab = EquipmentViewTab.BROWSE,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cloudAction = rememberCloudAction()
    val context = LocalContext.current
    var currentTab by remember(initialTab) { mutableStateOf(initialTab) }

    val equipmentList by repository.farmEquipmentList.collectAsState()
    val bookings by repository.equipmentBookings.collectAsState()
    val userLocation by repository.selectedLocation.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf(EquipmentCategory.ALL) }

    var selectedEquipmentForDetail by remember { mutableStateOf<FarmEquipment?>(null) }
    var selectedEquipmentForBooking by remember { mutableStateOf<FarmEquipment?>(null) }

    val filteredEquipment = remember(equipmentList, searchQuery, selectedCategoryFilter) {
        equipmentList.filter { eq ->
            val matchesCategory = selectedCategoryFilter == EquipmentCategory.ALL || eq.category == selectedCategoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                eq.nameEn.contains(searchQuery, ignoreCase = true) ||
                eq.nameHi.contains(searchQuery, ignoreCase = true) ||
                eq.providerName.contains(searchQuery, ignoreCase = true) ||
                eq.category.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    BackHandler {
        if (selectedEquipmentForDetail != null || selectedEquipmentForBooking != null) {
            selectedEquipmentForDetail = null
            selectedEquipmentForBooking = null
        } else if (currentTab != EquipmentViewTab.BROWSE) {
            currentTab = EquipmentViewTab.BROWSE
        } else {
            onNavigateBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
    ) {
        // Screen Top Header with Back Navigation
        Surface(
            color = if (isDarkMode) DarkCardSurface else TopBarBackground,
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("equipment_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚜", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> "Farm Equipment & Machinery"
                                    AppLanguage.HINDI -> "कृषि उपकरण एवं मशीनरी केंद्र"
                                    AppLanguage.MARATHI -> "शेती अवजारे व यंत्रसामग्री"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )
                        }
                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.ENGLISH -> "Book tractors, harvesters, rotavators & implements"
                                AppLanguage.HINDI -> "किराए पर ट्रैक्टर, हार्वेस्टर व उपकरण बुक करें"
                                AppLanguage.MARATHI -> "भाड्याने ट्रॅक्टर, हार्वेस्टर व अवजारे बुक करा"
                            },
                            fontSize = 11.sp,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )
                    }
                }

                // Tab Switcher (Browse Machinery / My Bookings)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EquipmentViewTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        val label = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> tab.labelEn
                            AppLanguage.HINDI -> tab.labelHi
                            AppLanguage.MARATHI -> tab.labelMr
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) {
                                if (isDarkMode) Color(0xFF263326) else LightGreenContainer
                            } else {
                                if (isDarkMode) DarkBackground else Color(0xFFF7F5F0)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ForestGreen else (if (isDarkMode) DarkCardBorder else CardBorderColor)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentTab = tab }
                                .testTag("equipment_tab_${tab.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (tab == EquipmentViewTab.MY_BOOKINGS) "$label (${bookings.size})" else label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ForestGreen else (if (isDarkMode) DarkTextSecondary else TextSecondaryMuted)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Content
        when (currentTab) {
            EquipmentViewTab.BROWSE -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search Bar
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search tractors, harvesters, rotavators...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TerracottaPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("equipment_search_input")
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
                            EquipmentCategory.values().forEach { cat ->
                                val isSelected = selectedCategoryFilter == cat
                                val label = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> cat.labelEn
                                    AppLanguage.HINDI -> cat.labelHi
                                    AppLanguage.MARATHI -> cat.labelMr
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) ForestGreen else (if (isDarkMode) DarkCardSurface else CardSurfaceWhite),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) ForestGreen else CardBorderColor
                                    ),
                                    modifier = Modifier.clickable { selectedCategoryFilter = cat }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else (if (isDarkMode) DarkTextPrimary else TextPrimaryDark),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (filteredEquipment.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🚜", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No machinery found for this search", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Try searching for 'Tractor', 'Rotavator', or 'Sprayer'", fontSize = 11.sp, color = TextSecondaryMuted)
                                }
                            }
                        }
                    } else {
                        items(filteredEquipment) { equipment ->
                            EquipmentCard(
                                equipment = equipment,
                                selectedLanguage = selectedLanguage,
                                isDarkMode = isDarkMode,
                                onViewDetails = { selectedEquipmentForDetail = equipment },
                                onBookNow = { selectedEquipmentForBooking = equipment }
                            )
                        }
                    }
                }
            }

            EquipmentViewTab.MY_BOOKINGS -> {
                MyBookingsTabContent(
                    bookings = bookings,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onCancelBooking = { id -> cloudAction.launch {
                        repository.cancelEquipmentBooking(id)
                        Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show()
                    } },
                    onBrowseMachinery = { currentTab = EquipmentViewTab.BROWSE }
                )
            }
        }
    }

    // Modal: Equipment Specifications & Terms Details
    selectedEquipmentForDetail?.let { equipment ->
        EquipmentDetailModal(
            equipment = equipment,
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onBookClick = {
                selectedEquipmentForDetail = null
                selectedEquipmentForBooking = equipment
            },
            onDismiss = { selectedEquipmentForDetail = null }
        )
    }

    // Modal: Booking Form
    selectedEquipmentForBooking?.let { equipment ->
        BookEquipmentDialog(
            equipment = equipment,
            userLocation = userLocation,
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onBookingConfirmed = { date, time, duration, location, instr, cost ->
                repository.bookEquipment(
                    equipmentId = equipment.id,
                    bookingDate = date,
                    timeSlot = time,
                    durationOrArea = duration,
                    farmLocation = location,
                    instructions = instr,
                    cost = cost
                )
                selectedEquipmentForBooking = null
                currentTab = EquipmentViewTab.MY_BOOKINGS
                Toast.makeText(context, "Booking request saved. Provider confirmation is pending.", Toast.LENGTH_LONG).show()
            },
            onDismiss = { selectedEquipmentForBooking = null }
        )
    }
}

@Composable
private fun EquipmentCard(
    equipment: FarmEquipment,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onViewDetails: () -> Unit,
    onBookNow: () -> Unit
) {
    val name = when (selectedLanguage) {
        AppLanguage.ENGLISH -> equipment.nameEn
        AppLanguage.HINDI -> equipment.nameHi
        AppLanguage.MARATHI -> equipment.nameMr
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails)
            .testTag("equipment_card_${equipment.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(equipment.iconEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )
                            if (equipment.isVerifiedProvider) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified Provider", tint = ForestGreen, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = "${equipment.providerName} • ${equipment.location}",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = equipment.availabilityStatus,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price & Specifications snippet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkMode) DarkBackground else Color(0xFFF9F7F2))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Rental Fare (Demo)", fontSize = 10.sp, color = TextSecondaryMuted)
                    Text(
                        text = "₹${equipment.priceAmount} / ${equipment.pricingUnit}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Distance", fontSize = 10.sp, color = TextSecondaryMuted)
                    Text("${equipment.distanceKm} km away", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Details", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onBookNow,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text("Book Equipment ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MyBookingsTabContent(
    bookings: List<EquipmentBooking>,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onCancelBooking: (String) -> Unit,
    onBrowseMachinery: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (bookings.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚜", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No equipment booked yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Browse available tractors and harvesters nearby to book machinery.", fontSize = 11.sp, color = TextSecondaryMuted)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onBrowseMachinery,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Browse Machinery", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(bookings) { booking ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(booking.iconEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(booking.equipmentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Booking ID: ${booking.id}", fontSize = 10.sp, color = TextSecondaryMuted)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (booking.status) {
                                    EquipmentBookingStatus.REQUESTED -> Color(0xFFFFF3E0)
                                    EquipmentBookingStatus.ACCEPTED,
                                    EquipmentBookingStatus.SCHEDULED -> Color(0xFFE8F5E9)
                                    EquipmentBookingStatus.COMPLETED -> Color(0xFFE0F2F1)
                                    EquipmentBookingStatus.CANCELLED -> Color(0xFFFFEBEE)
                                }
                            ) {
                                Text(
                                    text = when (selectedLanguage) {
                                        AppLanguage.ENGLISH -> booking.status.labelEn
                                        AppLanguage.HINDI -> booking.status.labelHi
                                        AppLanguage.MARATHI -> booking.status.labelMr
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (booking.status) {
                                        EquipmentBookingStatus.REQUESTED -> AlertOrange
                                        EquipmentBookingStatus.SCHEDULED,
                                        EquipmentBookingStatus.ACCEPTED,
                                        EquipmentBookingStatus.COMPLETED -> ForestGreen
                                        EquipmentBookingStatus.CANCELLED -> AlertRed
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("📅 Date: ${booking.bookingDate} (${booking.timeSlot})", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("⏳ Duration/Area: ${booking.durationOrArea} • Est. Cost: ₹${booking.estimatedCost}", fontSize = 11.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                        Text("📍 Farm: ${booking.farmLocation}", fontSize = 11.sp, color = TextSecondaryMuted)
                        Text("👤 Provider: ${booking.providerName} • 📞 ${booking.providerPhone}", fontSize = 11.sp, color = IndigoSecondary)

                        if (booking.status != EquipmentBookingStatus.COMPLETED && booking.status != EquipmentBookingStatus.CANCELLED) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { onCancelBooking(booking.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Cancel Booking", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Modal: Equipment Details
@Composable
private fun EquipmentDetailModal(
    equipment: FarmEquipment,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onBookClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val name = when (selectedLanguage) {
        AppLanguage.ENGLISH -> equipment.nameEn
        AppLanguage.HINDI -> equipment.nameHi
        AppLanguage.MARATHI -> equipment.nameMr
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(equipment.iconEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(equipment.providerName, fontSize = 11.sp, color = TextSecondaryMuted)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDarkMode) DarkBackground else Color(0xFFF9F7F2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Rental Fare (Demo Sample)", fontSize = 10.sp, color = TextSecondaryMuted)
                                Text("₹${equipment.priceAmount} / ${equipment.pricingUnit}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                                Text("📍 Location: ${equipment.location} (${equipment.distanceKm} km away)", fontSize = 11.sp, color = TextSecondaryMuted)
                            }
                        }
                    }

                    item {
                        Text("Overview & Description:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) equipment.descriptionHi else equipment.descriptionEn,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )
                    }

                    item {
                        Text("Technical Specifications:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            equipment.specifications.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(key, fontSize = 11.sp, color = TextSecondaryMuted)
                                    Text(value, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    item {
                        Text("Rental Terms & Conditions:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) equipment.termsHi else equipment.termsEn,
                            fontSize = 11.sp,
                            color = IndigoSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBookClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Proceed to Book Equipment ➔", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// Dialog: Book Equipment Form
@Composable
private fun BookEquipmentDialog(
    equipment: FarmEquipment,
    userLocation: String,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onBookingConfirmed: suspend (String, String, String, String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val cloudAction = rememberCloudAction()
    var bookingDate by remember { mutableStateOf("2026-09-30") }
    var timeSlot by remember { mutableStateOf("08:00 AM - 12:00 PM (Morning)") }
    var durationOrAreaText by remember { mutableStateOf("4") }
    var farmLocationText by remember { mutableStateOf(userLocation) }
    var instructionsText by remember { mutableStateOf("") }

    val durationVal = durationOrAreaText.toIntOrNull() ?: 1
    val estimatedTotal = durationVal * equipment.priceAmount

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Book Machinery", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(equipment.nameEn, fontSize = 11.sp, color = ForestGreen, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        OutlinedTextField(
                            value = bookingDate,
                            onValueChange = { bookingDate = it },
                            label = { Text("Select Booking Date (YYYY-MM-DD)") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = timeSlot,
                            onValueChange = { timeSlot = it },
                            label = { Text("Time Slot") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = durationOrAreaText,
                            onValueChange = { durationOrAreaText = it },
                            label = { Text("Duration (${equipment.pricingUnit}s) / Area") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = farmLocationText,
                            onValueChange = { farmLocationText = it },
                            label = { Text("Farm / Land Location") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = instructionsText,
                            onValueChange = { instructionsText = it },
                            label = { Text("Special Instructions for Operator") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Booking Summary Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDarkMode) Color(0xFF1E281E) else Color(0xFFE8F5E9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Booking Cost Summary:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ForestGreen)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rate: ₹${equipment.priceAmount} x $durationVal ${equipment.pricingUnit}s", fontSize = 11.sp)
                                    Text("Est. Total: ₹$estimatedTotal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                }
                                Text("No upfront payment needed. Pay provider after work completion.", fontSize = 9.sp, color = TextSecondaryMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { cloudAction.launch {
                        val durationFormatted = "$durationVal ${equipment.pricingUnit.capitalize(java.util.Locale.getDefault())}s"
                        onBookingConfirmed(
                            bookingDate,
                            timeSlot,
                            durationFormatted,
                            farmLocationText,
                            instructionsText,
                            estimatedTotal
                        )
                    } },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Confirm Booking Request ➔", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
