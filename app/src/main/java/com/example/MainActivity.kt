package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.NavDestination
import com.example.data.model.Scheme
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.components.DigitalSaathiBottomNav
import com.example.ui.components.DigitalSaathiTopBar
import com.example.ui.components.GlobalSearchDialog
import com.example.ui.components.SchemeDetailDialog
import com.example.ui.components.VoiceTtsHelper
import com.example.ui.screens.*
import com.example.ui.theme.DigitalSaathiTheme
import com.example.ui.theme.WarmCreamBackground
import com.example.ui.theme.DarkBackground

class MainActivity : ComponentActivity() {

    private var voiceHelper: VoiceTtsHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        runCatching {
            com.google.firebase.appcheck.FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                if (BuildConfig.DEBUG) com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
                else com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }


        voiceHelper = VoiceTtsHelper(this)

        setContent {
            DigitalSaathiApp(voiceHelper = voiceHelper)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper?.shutdown()
    }
}

@Composable
fun DigitalSaathiApp(
    voiceHelper: VoiceTtsHelper?,
    modifier: Modifier = Modifier
) {
    val repository = remember { DigitalSaathiRepository() }

    DisposableEffect(repository) { onDispose { repository.close() } }
    val cloudMessage by repository.cloudMessage.collectAsState()
    val cloudBusy by repository.cloudBusy.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(cloudMessage) { cloudMessage?.let { snackbar.showSnackbar(it); repository.clearCloudMessage() } }

    val currentDestination by repository.currentDestination.collectAsState()
    val selectedLanguage by repository.selectedLanguage.collectAsState()
    val userProfile by repository.userProfile.collectAsState()
    val weather by repository.weatherData.collectAsState()
    val isAuthenticated by repository.isAuthenticated.collectAsState()

    var isDarkMode by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var selectedSchemeForDetail by remember { mutableStateOf<Scheme?>(null) }

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) { isSearchOpen = false; selectedSchemeForDetail = null }
    }

    DigitalSaathiTheme(darkTheme = isDarkMode) {
        if (!isAuthenticated) {
            AuthScreen(
                repository = repository,
                selectedLanguage = selectedLanguage,
                isDarkMode = isDarkMode,
                onLanguageChange = { lang -> repository.setLanguage(lang) }
            )
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbar) },
                modifier = modifier.fillMaxSize(),
            topBar = {
                DigitalSaathiTopBar(
                    currentDestination = currentDestination,
                    selectedLanguage = selectedLanguage,
                    userProfile = userProfile,
                    isDarkMode = isDarkMode,
                    onDestinationSelected = { dest -> repository.navigateTo(dest) },
                    onLanguageSelected = { lang -> repository.setLanguage(lang) },
                    onToggleDarkMode = { isDarkMode = !isDarkMode },
                    onVoiceAssistClick = {
                        val speechSummary = when (currentDestination) {
                            NavDestination.HOME -> if (selectedLanguage == AppLanguage.HINDI) {
                                "नमस्ते ${userProfile.name}। डिजिटल साथी मुख्य पृष्ठ पर आपका स्वागत है। यहां आप सरकारी योजनाएं, खेती व मंडी भाव, और डिजिटल शिक्षा देख सकते हैं।"
                            } else {
                                "Namaste ${userProfile.name}. Welcome to Digital Saathi Home. Here you can access Government Schemes, Kheti Mandi rates, and Digital Education."
                            }
                            NavDestination.SERVICES -> if (selectedLanguage == AppLanguage.HINDI) {
                                "नागरिक सेवाएं। आधार कार्ड, राशन कार्ड, बिजली बिल और भूलेख 7 12 रिकॉर्ड यहां उपलब्ध हैं।"
                            } else {
                                "Citizen Services Directory. Aadhaar Card, Ration Card, Electricity Bill, and 7 12 Land Records are available here."
                            }
                            NavDestination.SCHEMES -> if (selectedLanguage == AppLanguage.HINDI) {
                                "सरकारी योजनाएं पुस्तकालय। किसान, महिला, छात्र और वरिष्ठ नागरिकों के लिए सब्सिडी और पेंशन योजनाएं।"
                            } else {
                                "Government Schemes Library. Financial assistance, subsidies, pensions and scholarships."
                            }
                            NavDestination.KHETI -> if (selectedLanguage == AppLanguage.HINDI) {
                                "खेती और मंडी भाव हब। मौसम और प्रकाशित मंडी भाव यहां देखें।"
                            } else {
                                "Kheti and Mandi Hub. Weather: ${if (weather.isLiveApi) "${weather.temperature} degrees" else "currently unavailable"}. Browse published mandi rates."
                            }
                            NavDestination.SHIKSHA -> if (selectedLanguage == AppLanguage.HINDI) {
                                "डिजिटल शिक्षा और सुरक्षा। यूपीआई सिम्युलेटर से सुरक्षित भुगतान सीखें और 1930 साइबर हेल्पलाइन याद रखें।"
                            } else {
                                "Digital Shiksha and Safety. Practice demo payments with UPI Simulator and learn cyber fraud prevention."
                            }
                            NavDestination.VILLAGE_SOS -> if (selectedLanguage == AppLanguage.HINDI) {
                                "${userProfile.village} ग्राम पोर्टल और आपातकालीन SOS नंबर। पुलिस 100, एम्बुलेंस 108, और किसान हेल्पलाइन 1551।"
                            } else {
                                "Village Portal ${userProfile.village} and SOS Emergency lines. Police 100, Ambulance 108, and Gram Panchayat Notice Board."
                            }
                            NavDestination.PROFILE -> if (selectedLanguage == AppLanguage.HINDI) {
                                "${userProfile.name} का प्रोफाइल। नागरिक जानकारी और सहेजे गए आवेदनों की स्थिति।"
                            } else {
                                "Profile of ${userProfile.name}. Citizen details and status of your saved applications."
                            }
                            NavDestination.SELL_PRODUCE -> if (selectedLanguage == AppLanguage.HINDI) {
                                "किसान फसल बिक्री केंद्र। अपनी फसल सीधे मंडी और सत्यापित खरीदारों को बेचें और परिवहन बुक करें।"
                            } else {
                                "Sell Farm Produce Hub. List crops, choose APMC mandis or direct buyers, and arrange delivery transport."
                            }
                            NavDestination.FARM_EQUIPMENT -> if (selectedLanguage == AppLanguage.HINDI) {
                                "कृषि उपकरण और मशीनरी केंद्र। किराए पर ट्रैक्टर, हार्वेस्टर और रोटावेटर बुक करें।"
                            } else {
                                "Farm Equipment and Machinery. Rent tractors, harvesters, rotavators, and implements."
                            }
                            NavDestination.MY_FARM_SALES,
                            NavDestination.DELIVERY_TRACKING -> if (selectedLanguage == AppLanguage.HINDI) {
                                "फसल बिक्री एवं डिलीवरी ट्रैकिंग।"
                            } else {
                                "Farm Sales and Delivery Tracking."
                            }
                            NavDestination.EQUIPMENT_BOOKINGS -> if (selectedLanguage == AppLanguage.HINDI) {
                                "मेरी कृषि उपकरण बुकिंग।"
                            } else {
                                "My Farm Equipment Bookings."
                            }
                        }
                        voiceHelper?.speak(speechSummary, selectedLanguage)
                    }
                )
            },
            bottomBar = {
                DigitalSaathiBottomNav(
                    currentDestination = currentDestination,
                    selectedLanguage = selectedLanguage,
                    isDarkMode = isDarkMode,
                    onDestinationSelected = { dest -> repository.navigateTo(dest) }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
            ) {
                if (cloudBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                when (currentDestination) {
                    NavDestination.HOME -> {
                        HomeScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            onNavigateTo = { dest -> repository.navigateTo(dest) },
                            onOpenSearch = { isSearchOpen = true },
                            onOpenSchemeDetail = { scheme -> selectedSchemeForDetail = scheme }
                        )
                    }
                    NavDestination.SERVICES -> {
                        ServicesScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = ServicesSubTab.ALL,
                            onSelectScheme = { scheme -> selectedSchemeForDetail = scheme },
                            onVoiceSpeak = { text -> voiceHelper?.speak(text, selectedLanguage) },
                            onNavigateBackToHome = { repository.navigateTo(NavDestination.HOME) }
                        )
                    }
                    NavDestination.SELL_PRODUCE -> {
                        SellProduceScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = SellProduceTab.MY_LISTINGS,
                            onNavigateBack = { repository.navigateTo(NavDestination.HOME) }
                        )
                    }
                    NavDestination.FARM_EQUIPMENT -> {
                        FarmEquipmentScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = EquipmentViewTab.BROWSE,
                            onNavigateBack = { repository.navigateTo(NavDestination.HOME) }
                        )
                    }
                    NavDestination.MY_FARM_SALES -> {
                        SellProduceScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = SellProduceTab.MY_LISTINGS,
                            onNavigateBack = { repository.navigateTo(NavDestination.SERVICES) }
                        )
                    }
                    NavDestination.DELIVERY_TRACKING -> {
                        SellProduceScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = SellProduceTab.DELIVERY_TRACK,
                            onNavigateBack = { repository.navigateTo(NavDestination.SERVICES) }
                        )
                    }
                    NavDestination.EQUIPMENT_BOOKINGS -> {
                        FarmEquipmentScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = EquipmentViewTab.MY_BOOKINGS,
                            onNavigateBack = { repository.navigateTo(NavDestination.SERVICES) }
                        )
                    }
                    NavDestination.SCHEMES -> {
                        ServicesScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = ServicesSubTab.SCHEMES,
                            onSelectScheme = { scheme -> selectedSchemeForDetail = scheme },
                            onVoiceSpeak = { text -> voiceHelper?.speak(text, selectedLanguage) },
                            onNavigateBackToHome = { repository.navigateTo(NavDestination.HOME) }
                        )
                    }
                    NavDestination.KHETI -> {
                        ServicesScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = ServicesSubTab.KHETI,
                            onSelectScheme = { scheme -> selectedSchemeForDetail = scheme },
                            onVoiceSpeak = { text -> voiceHelper?.speak(text, selectedLanguage) },
                            onNavigateBackToHome = { repository.navigateTo(NavDestination.HOME) }
                        )
                    }
                    NavDestination.SHIKSHA -> {
                        ServicesScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = ServicesSubTab.SHIKSHA,
                            onSelectScheme = { scheme -> selectedSchemeForDetail = scheme },
                            onVoiceSpeak = { text -> voiceHelper?.speak(text, selectedLanguage) },
                            onNavigateBackToHome = { repository.navigateTo(NavDestination.HOME) }
                        )
                    }
                    NavDestination.VILLAGE_SOS -> {
                        ServicesScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            initialTab = ServicesSubTab.SOS,
                            onSelectScheme = { scheme -> selectedSchemeForDetail = scheme },
                            onVoiceSpeak = { text -> voiceHelper?.speak(text, selectedLanguage) },
                            onNavigateBackToHome = { repository.navigateTo(NavDestination.HOME) }
                        )
                    }
                    NavDestination.PROFILE -> {
                        ProfileScreen(
                            repository = repository,
                            selectedLanguage = selectedLanguage,
                            isDarkMode = isDarkMode,
                            onLanguageChange = { lang -> repository.setLanguage(lang) }
                        )
                    }
                }
            }
        }

        // Global Search Dialog
        if (isSearchOpen) {
            GlobalSearchDialog(
                repository = repository,
                selectedLanguage = selectedLanguage,
                isDarkMode = isDarkMode,
                onDismiss = { isSearchOpen = false },
                onNavigateToDestination = { dest ->
                    repository.navigateTo(dest)
                }
            )
        }

        // Scheme Detail & Application Dialog
        selectedSchemeForDetail?.let { scheme ->
            SchemeDetailDialog(
                scheme = scheme,
                userProfile = userProfile,
                selectedLanguage = selectedLanguage,
                isDarkMode = isDarkMode,
                onBookmarkToggle = { repository.toggleSchemeBookmark(scheme.id) },
                onApplySubmitted = { s, name, phone, aadhaar ->
                    repository.submitSchemeApplication(s, name, phone, aadhaar).id
                },
                onDismiss = { selectedSchemeForDetail = null }
            )
        }
        }
    }
}
