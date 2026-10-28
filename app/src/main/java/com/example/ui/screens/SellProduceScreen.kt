package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.theme.*

enum class SellProduceTab(
    val labelEn: String,
    val labelHi: String,
    val labelMr: String
) {
    MY_LISTINGS("My Listings", "मेरी फसल सूची", "माझी यादी"),
    ADD_PRODUCE("+ Add Produce", "+ फसल जोड़ें", "+ शेतमाल जोडा"),
    BUYERS_MANDI("Buyers & Mandis", "मंडी व खरीदार", "बाजार व खरेदीदार"),
    DELIVERY_TRACK("Delivery Tracking", "परिवहन ट्रैकिंग", "डिलिव्हरी ट्रॅकिंग")
}

@Composable
fun SellProduceScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    initialTab: SellProduceTab = SellProduceTab.MY_LISTINGS,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cloudAction = rememberCloudAction()
    val context = LocalContext.current
    var currentTab by remember(initialTab) { mutableStateOf(initialTab) }

    val listings by repository.produceListings.collectAsState()
    val buyers by repository.produceBuyers.collectAsState()
    val deliveryPartners by repository.deliveryPartners.collectAsState()
    val deliveryRequests by repository.deliveryRequests.collectAsState()
    val userLocation by repository.selectedLocation.collectAsState()

    var selectedListingForBuyer by remember { mutableStateOf<ProduceListing?>(null) }
    var selectedListingForDelivery by remember { mutableStateOf<ProduceListing?>(null) }
    var selectedDeliveryRequestDetail by remember { mutableStateOf<DeliveryRequest?>(null) }

    BackHandler {
        if (selectedListingForBuyer != null || selectedListingForDelivery != null || selectedDeliveryRequestDetail != null) {
            selectedListingForBuyer = null
            selectedListingForDelivery = null
            selectedDeliveryRequestDetail = null
        } else if (currentTab != SellProduceTab.MY_LISTINGS) {
            currentTab = SellProduceTab.MY_LISTINGS
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
                        modifier = Modifier.testTag("sell_produce_back_button")
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
                            Text("🌾", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> "Sell Farm Produce"
                                    AppLanguage.HINDI -> "किसान फसल बिक्री केंद्र"
                                    AppLanguage.MARATHI -> "शेतकरी शेतीमाल विक्री केंद्र"
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
                                AppLanguage.ENGLISH -> "List crops, choose mandis, arrange delivery transport"
                                AppLanguage.HINDI -> "सीधे मंडी व सत्यापित खरीदारों को बेचें और वाहन पाएं"
                                AppLanguage.MARATHI -> "थेट बाजार समिती व खरेदीदारांना विका आणि वाहतूक मिळवा"
                            },
                            fontSize = 11.sp,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )
                    }
                }

                // Tab Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SellProduceTab.values().forEach { tab ->
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
                                .clickable { currentTab = tab }
                                .testTag("sell_tab_${tab.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
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
            SellProduceTab.MY_LISTINGS -> {
                MyListingsTabContent(
                    listings = listings,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onAddNewProduce = { currentTab = SellProduceTab.ADD_PRODUCE },
                    onChooseBuyer = { listing -> selectedListingForBuyer = listing },
                    onArrangeDelivery = { listing -> selectedListingForDelivery = listing },
                    onTrackDelivery = { request -> selectedDeliveryRequestDetail = request },
                    onDeleteListing = { id -> cloudAction.launch {
                        repository.deleteProduceListing(id)
                        Toast.makeText(context, "Listing removed", Toast.LENGTH_SHORT).show()
                    } }
                )
            }
            SellProduceTab.ADD_PRODUCE -> {
                AddProduceFormContent(
                    repository = repository,
                    userLocation = userLocation,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onListingCreated = { listing ->
                        Toast.makeText(context, "🌾 Produce listed successfully!", Toast.LENGTH_SHORT).show()
                        selectedListingForBuyer = listing
                        currentTab = SellProduceTab.MY_LISTINGS
                    }
                )
            }
            SellProduceTab.BUYERS_MANDI -> {
                BuyersAndMandiTabContent(
                    buyers = buyers,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onSelectBuyer = { buyer ->
                        Toast.makeText(context, "Selected ${buyer.name}. Link with your active produce listing.", Toast.LENGTH_SHORT).show()
                        currentTab = SellProduceTab.MY_LISTINGS
                    }
                )
            }
            SellProduceTab.DELIVERY_TRACK -> {
                DeliveryTrackingTabContent(
                    deliveryRequests = deliveryRequests,
                    deliveryPartners = deliveryPartners,
                    repository = repository,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode
                )
            }
        }
    }

    // Modal: Choose Buyer / Mandi for a specific produce listing
    selectedListingForBuyer?.let { listing ->
        ChooseBuyerDialog(
            listing = listing,
            buyers = buyers,
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onBuyerSelected = { buyer -> cloudAction.launch {
                repository.selectBuyerForProduce(listing.id, buyer)
                selectedListingForBuyer = null
                selectedListingForDelivery = listing.copy(selectedBuyer = buyer)
                Toast.makeText(context, "Buyer ${buyer.name} selected! Now arrange transportation.", Toast.LENGTH_SHORT).show()
            } },
            onDismiss = { selectedListingForBuyer = null }
        )
    }

    // Modal: Arrange Delivery for a listing
    selectedListingForDelivery?.let { listing ->
        ArrangeDeliveryDialog(
            listing = listing,
            deliveryPartners = deliveryPartners,
            userLocation = userLocation,
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onDeliveryRequested = { partner, isSelf, pickDate, pickTime, vType, instr, fare ->
                val dest = listing.selectedBuyer?.name ?: "Nearby Mandi Yard"
                val req = repository.requestDelivery(
                    produceId = listing.id,
                    deliveryPartner = partner,
                    isSelfTransport = isSelf,
                    pickupLocation = listing.pickupLocation,
                    destination = dest,
                    pickupDate = pickDate,
                    pickupTime = pickTime,
                    vehicleType = vType,
                    instructions = instr,
                    estimatedFare = fare
                )
                selectedListingForDelivery = null
                selectedDeliveryRequestDetail = req
                Toast.makeText(context, "Delivery request saved. Transport confirmation is pending.", Toast.LENGTH_LONG).show()
            },
            onDismiss = { selectedListingForDelivery = null }
        )
    }

    // Modal: Full Delivery Status Step Tracker
    selectedDeliveryRequestDetail?.let { request ->
        val liveRequest = deliveryRequests.find { it.id == request.id } ?: request
        DeliveryStatusTrackerDialog(
            request = liveRequest,
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onAdvanceStatus = { repository.advanceDeliveryStatus(liveRequest.id) },
            onDismiss = { selectedDeliveryRequestDetail = null }
        )
    }
}

@Composable
private fun MyListingsTabContent(
    listings: List<ProduceListing>,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onAddNewProduce: () -> Unit,
    onChooseBuyer: (ProduceListing) -> Unit,
    onArrangeDelivery: (ProduceListing) -> Unit,
    onTrackDelivery: (DeliveryRequest) -> Unit,
    onDeleteListing: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) Color(0xFF1E281E) else Color(0xFFF1F8F1),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF334A33) else Color(0xFFC5E1A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.ENGLISH -> "Sell Your Harvest Directly"
                                AppLanguage.HINDI -> "सीधे अपनी फसल बेचें"
                                AppLanguage.MARATHI -> "आपला शेतीमाल थेट विका"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ForestGreen
                        )
                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.ENGLISH -> "Connect with APMC mandis, direct mills, and book delivery transport."
                                AppLanguage.HINDI -> "मंडी और खरीदारों से जुड़ें व वाहन चालक बुक करें।"
                                AppLanguage.MARATHI -> "बाजार समिती व खरेदीदारांशी जोडा आणि वाहन बुक करा."
                            },
                            fontSize = 11.sp,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )
                    }

                    Button(
                        onClick = onAddNewProduce,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("add_produce_primary_btn")
                    ) {
                        Text("+ Add Produce", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (listings.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌾", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No produce listed yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )
                        Text(
                            text = "Tap '+ Add Produce' to create your first harvest listing for buyers.",
                            fontSize = 12.sp,
                            color = TextSecondaryMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(listings) { listing ->
                ProduceListingCard(
                    listing = listing,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onChooseBuyer = { onChooseBuyer(listing) },
                    onArrangeDelivery = { onArrangeDelivery(listing) },
                    onTrackDelivery = { listing.deliveryRequest?.let { req -> onTrackDelivery(req) } },
                    onDelete = { onDeleteListing(listing.id) }
                )
            }
        }
    }
}

@Composable
private fun ProduceListingCard(
    listing: ProduceListing,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onChooseBuyer: () -> Unit,
    onArrangeDelivery: () -> Unit,
    onTrackDelivery: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("produce_card_${listing.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Crop icon + Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(listing.productPhoto, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = listing.cropName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )
                        Text(
                            text = "Category: ${listing.category} • Grade: ${listing.qualityGrade}",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (listing.status) {
                        ProduceSaleStatus.ACTIVE_LISTING -> Color(0xFFE3F2FD)
                        ProduceSaleStatus.BUYER_SELECTED -> Color(0xFFFFF3E0)
                        ProduceSaleStatus.DELIVERY_REQUESTED,
                        ProduceSaleStatus.DELIVERY_ACCEPTED,
                        ProduceSaleStatus.PICKUP_SCHEDULED,
                        ProduceSaleStatus.PICKED_UP,
                        ProduceSaleStatus.IN_TRANSIT -> Color(0xFFE8F5E9)
                        ProduceSaleStatus.DELIVERED,
                        ProduceSaleStatus.COMPLETED -> Color(0xFFE0F2F1)
                    }
                ) {
                    Text(
                        text = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> listing.status.labelEn
                            AppLanguage.HINDI -> listing.status.labelHi
                            AppLanguage.MARATHI -> listing.status.labelMr
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (listing.status) {
                            ProduceSaleStatus.ACTIVE_LISTING -> IndigoSecondary
                            ProduceSaleStatus.BUYER_SELECTED -> AlertOrange
                            ProduceSaleStatus.COMPLETED -> ForestGreen
                            else -> DarkGreenText
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Row: Quantity, Expected Price, Harvest Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDarkMode) DarkBackground else Color(0xFFF9F7F2))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Quantity", fontSize = 10.sp, color = TextSecondaryMuted)
                    Text("${listing.quantity} ${listing.unit}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Expected Price", fontSize = 10.sp, color = TextSecondaryMuted)
                    Text("₹${String.format(java.util.Locale.US, "%.0f", listing.expectedPricePerUnit)} / Qtl", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                }
                Column {
                    Text("Harvest Date", fontSize = 10.sp, color = TextSecondaryMuted)
                    Text(listing.harvestDate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Buyer Info if assigned
            listing.selectedBuyer?.let { buyer ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkMode) Color(0xFF282834) else Color(0xFFEEF2FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = IndigoSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Buyer / Mandi: ${buyer.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                            Text("${buyer.location} • Rate: ₹${buyer.offeredPricePerUnit?.toInt() ?: 0}/Qtl (Demo)", fontSize = 10.sp, color = TextSecondaryMuted)
                        }
                    }
                }
            }

            // Delivery Status if requested
            listing.deliveryRequest?.let { delReq ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkMode) Color(0xFF1E281E) else Color(0xFFE8F5E9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTrackDelivery)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Delivery: ${delReq.status.labelEn}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                Text("${delReq.vehicleType} • Fare: ₹${delReq.estimatedFare}", fontSize = 10.sp, color = TextSecondaryMuted)
                            }
                        }
                        Text("Track ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (listing.selectedBuyer == null) {
                    Button(
                        onClick = onChooseBuyer,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Text("Choose Buyer / Mandi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (listing.deliveryRequest == null) {
                    Button(
                        onClick = onArrangeDelivery,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Arrange Delivery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onTrackDelivery,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Delivery Tracker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Delete Icon
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AlertRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProduceFormContent(
    repository: DigitalSaathiRepository,
    userLocation: String,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onListingCreated: (ProduceListing) -> Unit
) {
    val cloudAction = rememberCloudAction()
    val commonCrops = listOf(
        "Soybean (सोयाबीन)",
        "Wheat (गेहूं)",
        "Cotton (कपास)",
        "Gram / Chana (चना)",
        "Tur / Arhar (तूर)",
        "Mustard (सरसों)",
        "Maize (मक्का)",
        "Onion (प्याज)",
        "Potato (आलू)",
        "Tomato & Vegetables",
        "Apple & Fruits",
        "Sugarcane (गन्ना)",
        "Other Agri Produce"
    )

    val categories = listOf("Grains & Cereals", "Oilseeds", "Pulses", "Fibers & Commercial", "Vegetables & Fruits")
    val units = listOf("Quintal (क्विंटल)", "Metric Ton (टन)", "Bags (बोरियां)", "Kg (किलो)")
    val grades = listOf("Grade A (Super Clean)", "Grade B (Standard)", "Grade C (Fair)", "Organic Certified")

    var selectedCrop by remember { mutableStateOf(commonCrops[0]) }
    var customCropName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var quantityText by remember { mutableStateOf("50") }
    var selectedUnit by remember { mutableStateOf(units[0]) }
    var expectedPriceText by remember { mutableStateOf("4800") }
    var selectedGrade by remember { mutableStateOf(grades[0]) }
    var harvestDateText by remember { mutableStateOf("2026-09-28") }
    var pickupLocationText by remember { mutableStateOf(userLocation) }
    var preferredMandiText by remember { mutableStateOf("Nearby APMC Mandi") }
    var notesText by remember { mutableStateOf("") }

    var cropDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "🌾 Add Agricultural Harvest Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ForestGreen
                    )

                    // Crop Selection Dropdown
                    ExposedDropdownMenuBox(
                        expanded = cropDropdownExpanded,
                        onExpandedChange = { cropDropdownExpanded = !cropDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCrop,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Crop / Product *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropDropdownExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = cropDropdownExpanded,
                            onDismissRequest = { cropDropdownExpanded = false }
                        ) {
                            commonCrops.forEach { crop ->
                                DropdownMenuItem(
                                    text = { Text(crop) },
                                    onClick = {
                                        selectedCrop = crop
                                        cropDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCrop.contains("Other")) {
                        OutlinedTextField(
                            value = customCropName,
                            onValueChange = { customCropName = it },
                            label = { Text("Enter Custom Crop Name") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Quantity and Unit Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Quantity *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        var unitExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = !unitExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = selectedUnit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            selectedUnit = u
                                            unitExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Expected Price & Grade
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = expectedPriceText,
                            onValueChange = { expectedPriceText = it },
                            label = { Text("Expected Price (₹/Unit) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        var gradeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = gradeExpanded,
                            onExpandedChange = { gradeExpanded = !gradeExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = selectedGrade,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Quality / Grade") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = gradeExpanded,
                                onDismissRequest = { gradeExpanded = false }
                            ) {
                                grades.forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g) },
                                        onClick = {
                                            selectedGrade = g
                                            gradeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Harvest Date & Pickup Location
                    OutlinedTextField(
                        value = harvestDateText,
                        onValueChange = { harvestDateText = it },
                        label = { Text("Harvest Date (YYYY-MM-DD)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pickupLocationText,
                        onValueChange = { pickupLocationText = it },
                        label = { Text("Farm / Village Pickup Location *") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TerracottaPrimary) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = preferredMandiText,
                        onValueChange = { preferredMandiText = it },
                        label = { Text("Preferred Mandi or Buyer") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Additional Notes (Moisture %, packaging, etc.)") },
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { cloudAction.launch {
                            val finalCropName = if (selectedCrop.contains("Other") && customCropName.isNotBlank()) customCropName else selectedCrop
                            val qty = quantityText.toDoubleOrNull() ?: error("Enter a valid quantity.")
                            val price = expectedPriceText.toDoubleOrNull() ?: error("Enter a valid price.")

                            val newListing = repository.addProduceListing(
                                cropName = finalCropName,
                                category = selectedCategory,
                                quantity = qty,
                                unit = selectedUnit,
                                expectedPricePerUnit = price,
                                qualityGrade = selectedGrade,
                                harvestDate = harvestDateText,
                                pickupLocation = pickupLocationText,
                                preferredBuyerOrMandi = preferredMandiText,
                                notes = notesText,
                                productPhoto = when {
                                    finalCropName.contains("Soybean", ignoreCase = true) -> "🌱"
                                    finalCropName.contains("Wheat", ignoreCase = true) -> "🌾"
                                    finalCropName.contains("Cotton", ignoreCase = true) -> "☁️"
                                    finalCropName.contains("Chana", ignoreCase = true) -> "🫘"
                                    finalCropName.contains("Onion", ignoreCase = true) -> "🧅"
                                    finalCropName.contains("Fruit", ignoreCase = true) -> "🍎"
                                    else -> "🌾"
                                }
                            )
                            onListingCreated(newListing)
                        } },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_add_produce_btn")
                    ) {
                        Text("List Produce & Choose Buyer ➔", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyersAndMandiTabContent(
    buyers: List<ProduceBuyer>,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onSelectBuyer: (ProduceBuyer) -> Unit
) {
    var selectedTypeFilter by remember { mutableStateOf("ALL") }

    val filteredBuyers = remember(buyers, selectedTypeFilter) {
        if (selectedTypeFilter == "ALL") buyers
        else buyers.filter { it.type.name == selectedTypeFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All Buyers",
                    "MANDI" to "Nearby APMC Mandis",
                    "DIRECT_BUYER" to "Direct Mills & Buyers",
                    "VERIFIED_BUYER" to "Verified FPOs"
                ).forEach { (typeKey, label) ->
                    val isSelected = selectedTypeFilter == typeKey
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) IndigoSecondary else (if (isDarkMode) DarkCardSurface else CardSurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) IndigoSecondary else CardBorderColor),
                        modifier = Modifier.clickable { selectedTypeFilter = typeKey }
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

        items(filteredBuyers) { buyer ->
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
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE7F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = IndigoSecondary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = buyer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                    )
                                    if (buyer.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = ForestGreen, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(
                                    text = "${buyer.location} • ${buyer.distanceKm} km away",
                                    fontSize = 11.sp,
                                    color = TextSecondaryMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (buyer.type == BuyerType.MANDI) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = when (buyer.type) {
                                    BuyerType.MANDI -> "APMC Mandi"
                                    BuyerType.DIRECT_BUYER -> "Direct Mill"
                                    BuyerType.VERIFIED_BUYER -> "Verified FPO"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (buyer.type == BuyerType.MANDI) ForestGreen else AlertOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Crops Accepted: ${buyer.cropsAccepted.joinToString(", ")}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = IndigoSecondary
                    )

                    buyer.offeredPricePerUnit?.let { price ->
                        Text(
                            text = "Sample Buying Rate: ₹${price.toInt()} / Qtl (Demo Price)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📞 ${buyer.contactPhone} • ⭐ ${buyer.rating}",
                            fontSize = 10.sp,
                            color = TextSecondaryMuted
                        )

                        Button(
                            onClick = { onSelectBuyer(buyer) },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Select Buyer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryTrackingTabContent(
    deliveryRequests: List<DeliveryRequest>,
    deliveryPartners: List<DeliveryPartner>,
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean
) {
    var selectedRequestForTracker by remember { mutableStateOf<DeliveryRequest?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "🚚 Active Delivery Shipments",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
            )
        }

        if (deliveryRequests.isEmpty()) {
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
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active deliveries yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Select a produce listing to arrange pickup & delivery transport.", fontSize = 11.sp, color = TextSecondaryMuted)
                    }
                }
            }
        } else {
            items(deliveryRequests) { request ->
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
                            Column {
                                Text(
                                    text = request.produceTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                )
                                Text("Quantity: ${request.quantity} • Request ID: ${request.id}", fontSize = 11.sp, color = TextSecondaryMuted)
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = request.status.labelEn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("📍 Pickup: ${request.pickupLocation}", fontSize = 11.sp)
                        Text("🎯 Destination: ${request.destination}", fontSize = 11.sp)
                        Text("🚛 Vehicle: ${request.vehicleType} • Fare: ₹${request.estimatedFare}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = ForestGreen)

                        request.deliveryPartner?.let { dp ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("👤 Driver: ${dp.driverName} (${dp.vehicleNumber}) • 📞 ${dp.phone}", fontSize = 10.sp, color = IndigoSecondary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { selectedRequestForTracker = request },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Open Step Tracker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { repository.advanceDeliveryStatus(request.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Advance Demo Step ➔", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "🚜 Registered Delivery Transport Partners",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
            )
        }

        items(deliveryPartners) { partner ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(partner.driverName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (partner.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(13.dp))
                            }
                        }
                        Text("${partner.vehicleType} (${partner.vehicleNumber})", fontSize = 11.sp, color = IndigoSecondary)
                        Text("${partner.serviceArea} • Base Fare: ₹${partner.estimatedFare}", fontSize = 10.sp, color = TextSecondaryMuted)
                    }
                    Text("⭐ ${partner.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                }
            }
        }
    }

    selectedRequestForTracker?.let { req ->
        val live = deliveryRequests.find { it.id == req.id } ?: req
        DeliveryStatusTrackerDialog(
            request = live,
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onAdvanceStatus = { repository.advanceDeliveryStatus(live.id) },
            onDismiss = { selectedRequestForTracker = null }
        )
    }
}

// Dialog: Choose Buyer / Mandi
@Composable
private fun ChooseBuyerDialog(
    listing: ProduceListing,
    buyers: List<ProduceBuyer>,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onBuyerSelected: (ProduceBuyer) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Select Buyer or Mandi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("For ${listing.cropName} (${listing.quantity} ${listing.unit})", fontSize = 11.sp, color = TextSecondaryMuted)
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
                    items(buyers) { buyer ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) DarkBackground else Color(0xFFF9F7F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBuyerSelected(buyer) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(buyer.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${buyer.location} • ${buyer.distanceKm} km", fontSize = 10.sp, color = TextSecondaryMuted)
                                    }
                                    Text("⭐ ${buyer.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Offered Price: ₹${buyer.offeredPricePerUnit?.toInt() ?: 0} / Qtl (Demo)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { onBuyerSelected(buyer) },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Sell to this Buyer / Mandi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Arrange Delivery Transport
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArrangeDeliveryDialog(
    listing: ProduceListing,
    deliveryPartners: List<DeliveryPartner>,
    userLocation: String,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onDeliveryRequested: suspend (DeliveryPartner?, Boolean, String, String, String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val cloudAction = rememberCloudAction()
    var deliveryOption by remember { mutableStateOf(2) } // 1: Self, 2: Find Delivery Partner
    var selectedPartner by remember { mutableStateOf(deliveryPartners.firstOrNull()) }
    var pickupDate by remember { mutableStateOf("2026-09-30") }
    var pickupTime by remember { mutableStateOf("09:00 AM - 12:00 PM") }
    var instructions by remember { mutableStateOf("") }

    val destinationName = listing.selectedBuyer?.name ?: "Nearby APMC Mandi"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Arrange Delivery Transport", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("To: $destinationName", fontSize = 11.sp, color = ForestGreen, fontWeight = FontWeight.SemiBold)
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
                        Text("Select Transportation Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (deliveryOption == 2) Color(0xFFE8F5E9) else (if (isDarkMode) DarkBackground else Color(0xFFF7F5F0)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (deliveryOption == 2) ForestGreen else CardBorderColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { deliveryOption = 2 }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("🚚 Find Delivery Partner", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (deliveryOption == 2) ForestGreen else TextPrimaryDark)
                                    Text("Tractor / Pickup / Truck", fontSize = 9.sp, color = TextSecondaryMuted)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (deliveryOption == 1) Color(0xFFE0E5FF) else (if (isDarkMode) DarkBackground else Color(0xFFF7F5F0)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (deliveryOption == 1) IndigoSecondary else CardBorderColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { deliveryOption = 1 }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("🚜 Self Transport", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (deliveryOption == 1) IndigoSecondary else TextPrimaryDark)
                                    Text("I will arrange my vehicle", fontSize = 9.sp, color = TextSecondaryMuted)
                                }
                            }
                        }
                    }

                    if (deliveryOption == 2) {
                        item {
                            Text("Available Delivery Partners:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        items(deliveryPartners) { partner ->
                            val isSelected = selectedPartner?.id == partner.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFE8F5E9) else (if (isDarkMode) DarkBackground else Color(0xFFF9F7F2)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ForestGreen else CardBorderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPartner = partner }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = { selectedPartner = partner })
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(partner.driverName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${partner.vehicleType} • ${partner.availability}", fontSize = 10.sp, color = IndigoSecondary)
                                    }
                                    Text("₹${partner.estimatedFare}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = pickupDate,
                            onValueChange = { pickupDate = it },
                            label = { Text("Preferred Pickup Date") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = pickupTime,
                            onValueChange = { pickupTime = it },
                            label = { Text("Preferred Time Slot") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = instructions,
                            onValueChange = { instructions = it },
                            label = { Text("Driver Pickup Instructions (e.g. Farm gate #2)") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { cloudAction.launch {
                        val isSelf = deliveryOption == 1
                        val partner = if (isSelf) null else selectedPartner
                        val fare = if (isSelf) 0 else (selectedPartner?.estimatedFare ?: 650)
                        val vType = if (isSelf) "Farmer Own Transport" else (selectedPartner?.vehicleType ?: "Tractor Trolley")

                        onDeliveryRequested(
                            partner,
                            isSelf,
                            pickupDate,
                            pickupTime,
                            vType,
                            instructions,
                            fare
                        )
                    } },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Confirm & Request Delivery ➔", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// Dialog: Delivery Status Step Tracker
@Composable
private fun DeliveryStatusTrackerDialog(
    request: DeliveryRequest,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onAdvanceStatus: () -> Unit,
    onDismiss: () -> Unit
) {
    val statuses = listOf(
        DeliveryStatus.REQUESTED,
        DeliveryStatus.ACCEPTED,
        DeliveryStatus.PICKUP_SCHEDULED,
        DeliveryStatus.PICKED_UP,
        DeliveryStatus.IN_TRANSIT,
        DeliveryStatus.DELIVERED
    )

    val currentStep = request.status.stepIndex

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Delivery Tracking", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("ID: ${request.id}", fontSize = 10.sp, color = TextSecondaryMuted)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDarkMode) DarkBackground else Color(0xFFF9F7F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🌾 ${request.produceTitle} (${request.quantity})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("📍 ${request.pickupLocation} ➔ ${request.destination}", fontSize = 10.sp, color = IndigoSecondary)
                        request.deliveryPartner?.let { dp ->
                            Text("👤 Driver: ${dp.driverName} • ${dp.phone}", fontSize = 10.sp, color = TextSecondaryMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Status Progress Tracker:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                // Vertical Step Tracker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    statuses.forEachIndexed { index, status ->
                        val isDone = index <= currentStep
                        val isCurrent = index == currentStep

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> ForestGreen
                                            isDone -> LightGreenContainer
                                            else -> Color(0xFFE0E0E0)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = if (isCurrent) Color.White else DarkGreenText, modifier = Modifier.size(14.dp))
                                } else {
                                    Text("${index + 1}", fontSize = 10.sp, color = Color.DarkGray)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = status.labelEn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else (if (isDone) FontWeight.Medium else FontWeight.Normal),
                                    color = if (isCurrent) ForestGreen else (if (isDone) (if (isDarkMode) DarkTextPrimary else TextPrimaryDark) else TextSecondaryMuted)
                                )
                                if (isCurrent) {
                                    Text("Updated: ${request.statusUpdatedAt}", fontSize = 9.sp, color = ForestGreen)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAdvanceStatus,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Advance Next Step (Demo Simulation) ➔", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}
