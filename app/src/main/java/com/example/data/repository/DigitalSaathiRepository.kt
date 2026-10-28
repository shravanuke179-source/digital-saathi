package com.example.data.repository

import com.example.data.model.*
import com.example.data.network.KhetiMarketApiService
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DigitalSaathiRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val khetiMarketService = KhetiMarketApiService()

    private val _selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _currentDestination = MutableStateFlow(NavDestination.HOME)
    val currentDestination: StateFlow<NavDestination> = _currentDestination.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _schemes = MutableStateFlow(defaultSchemes)
    val schemes: StateFlow<List<Scheme>> = _schemes.asStateFlow()

    private val _mandiPrices = MutableStateFlow<List<MandiPrice>>(emptyList())
    val mandiPrices: StateFlow<List<MandiPrice>> = _mandiPrices.asStateFlow()

    private val _weatherData = MutableStateFlow(
        WeatherData(
            locationName = "Palampur, Himachal Pradesh",
            temperature = 28.4,
            feelsLike = 29.8,
            humidity = 62,
            windSpeed = 11.5,
            weatherCode = 1,
            conditionEn = "Partly Cloudy",
            conditionHi = "आंशिक रूप से बादल",
            conditionMr = "अंशतः ढगाळ",
            iconEmoji = "🌤️",
            maxTemp = 32.0,
            minTemp = 21.5,
            rainProbability = 15,
            farmingAdviceEn = "Good weather for field preparation, sowing, and irrigation.",
            farmingAdviceHi = "खेत की तैयारी, बुआई और हल्की सिंचाई के लिए उत्तम मौसम।",
            farmingAdviceMr = "शेताची तयारी, पेरणी आणि सिंचनासाठी उत्तम हवामान.",
            lastUpdatedTime = "Sample — waiting for weather",
            isLiveApi = false
        )
    )
    val weatherData: StateFlow<WeatherData> = _weatherData.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    private val _isMandiLoading = MutableStateFlow(false)
    val isMandiLoading: StateFlow<Boolean> = _isMandiLoading.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _supabaseSyncStatus = MutableStateFlow(
        SupabaseSyncStatus(
            isConnected = false,
            isSyncing = false,
            lastSyncMessage = "Firebase not connected",
            tableName = "users/{uid}",
            recordsCount = 0
        )
    )
    val supabaseSyncStatus: StateFlow<SupabaseSyncStatus> = _supabaseSyncStatus.asStateFlow()

    private val _notices = MutableStateFlow<List<VillageNotice>>(emptyList())
    val notices: StateFlow<List<VillageNotice>> = _notices.asStateFlow()

    private val _submittedApplications = MutableStateFlow<List<SubmittedApplication>>(emptyList())
    val submittedApplications: StateFlow<List<SubmittedApplication>> = _submittedApplications.asStateFlow()

    private val _grievances = MutableStateFlow<List<PanchayatGrievance>>(emptyList())
    val grievances: StateFlow<List<PanchayatGrievance>> = _grievances.asStateFlow()

    private val _selectedLocation = MutableStateFlow("Palampur, Himachal Pradesh")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    private val _userCoordinates = MutableStateFlow(Pair(32.1109, 76.5363))
    val userCoordinates: StateFlow<Pair<Double, Double>> = _userCoordinates.asStateFlow()

    private val _detectedState = MutableStateFlow("Himachal Pradesh")
    val detectedState: StateFlow<String> = _detectedState.asStateFlow()

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    // Farmer Services: Produce & Equipment StateFlows
    private val _produceListings = MutableStateFlow<List<ProduceListing>>(emptyList())
    val produceListings: StateFlow<List<ProduceListing>> = _produceListings.asStateFlow()

    private val _produceBuyers = MutableStateFlow<List<ProduceBuyer>>(emptyList())
    val produceBuyers: StateFlow<List<ProduceBuyer>> = _produceBuyers.asStateFlow()

    private val _deliveryPartners = MutableStateFlow<List<DeliveryPartner>>(emptyList())
    val deliveryPartners: StateFlow<List<DeliveryPartner>> = _deliveryPartners.asStateFlow()

    private val _deliveryRequests = MutableStateFlow<List<DeliveryRequest>>(emptyList())
    val deliveryRequests: StateFlow<List<DeliveryRequest>> = _deliveryRequests.asStateFlow()

    private val _farmEquipmentList = MutableStateFlow<List<FarmEquipment>>(emptyList())
    val farmEquipmentList: StateFlow<List<FarmEquipment>> = _farmEquipmentList.asStateFlow()

    private val _equipmentBookings = MutableStateFlow<List<EquipmentBooking>>(emptyList())
    val equipmentBookings: StateFlow<List<EquipmentBooking>> = _equipmentBookings.asStateFlow()

    val citizenServices: List<CitizenService> = defaultCitizenServices
    val digitalLessons: List<DigitalLesson> = defaultDigitalLessons
    val emergencyContacts: List<EmergencyContact> = defaultEmergencyContacts
    val quizQuestions: List<QuizQuestion> = defaultQuizQuestions
    val cropAdvisories: List<CropAdvisory> = defaultCropAdvisories

    private val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
    private val database = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    private val store = database?.let { CloudStore(it) }
    private val listeners = mutableListOf<ListenerRegistration>()
    private val mutationLock = Mutex()
    private val _cloudMessage = MutableStateFlow<String?>(null)
    val cloudMessage: StateFlow<String?> = _cloudMessage.asStateFlow()
    private val _cloudBusy = MutableStateFlow(false)
    val cloudBusy: StateFlow<Boolean> = _cloudBusy.asStateFlow()
    private var sessionUid: String? = null
    private val authListener = FirebaseAuth.AuthStateListener { firebase ->
        val user = firebase.currentUser
        if (user?.uid != sessionUid || user == null) {
            clearSession()
            if (user != null) {
                sessionUid = user.uid
                _userProfile.value = UserProfile(name = user.displayName.orEmpty(), email = user.email.orEmpty(),
                    firebaseUid = user.uid, authProvider = user.providerData.lastOrNull()?.providerId.orEmpty())
                _isAuthenticated.value = true
                watchUser(user.uid)
            }
        }
    }

    private fun clearSession() {
        listeners.forEach { it.remove() }
        listeners.clear()
        sessionUid = null
        _isAuthenticated.value = false
        _userProfile.value = UserProfile()
        _currentDestination.value = NavDestination.HOME
        _submittedApplications.value = emptyList()
        _grievances.value = emptyList()
        _produceListings.value = emptyList()
        _deliveryRequests.value = emptyList()
        _equipmentBookings.value = emptyList()
        _notices.value = emptyList()
        _produceBuyers.value = emptyList()
        _deliveryPartners.value = emptyList()
        _farmEquipmentList.value = emptyList()
        _mandiPrices.value = emptyList()
        _schemes.value = defaultSchemes.map { it.copy(isBookmarked = false) }
        _selectedLocation.value = "Palampur, Himachal Pradesh"
        _userCoordinates.value = Pair(32.1109, 76.5363)
        _isGpsActive.value = false
        _detectedState.value = "Himachal Pradesh"
        _selectedLanguage.value = AppLanguage.ENGLISH
        _cloudMessage.value = null
        _supabaseSyncStatus.value = SupabaseSyncStatus(false, false, "Signed out", "users/{uid}", 0)
    }

    private fun watchUser(uid: String) {
        val db = database ?: return
        val cloud = store ?: return
        listeners += db.document("users/$uid").addSnapshotListener { document, error ->
            if (auth?.currentUser?.uid == uid) {
                if (error != null) cloudFailure(error)
                else if (document != null && document.exists()) {
                    _userProfile.value = _userProfile.value.copy(
                        name = document.getString("name").orEmpty(), village = document.getString("village").orEmpty(),
                        district = document.getString("district").orEmpty(), phone = document.getString("phone").orEmpty(),
                        state = document.getString("state").orEmpty(), isVerified = document.getBoolean("isVerified") ?: false,
                        rationCardNo = document.getString("rationCardNo").orEmpty(), bankLinked = document.getString("bankLinked").orEmpty(),
                        isSupabaseSynced = !document.metadata.hasPendingWrites() && !document.metadata.isFromCache,
                        lastSyncedWithSupabase = if (document.metadata.isFromCache) "Cached" else "Synced with Firestore")
                    _supabaseSyncStatus.value = SupabaseSyncStatus(!document.metadata.isFromCache, false,
                        if (document.metadata.isFromCache) "Offline cached profile" else "Connected to Firestore", "users/$uid", 1)
                } else if (document != null && !document.metadata.isFromCache) {
                    // Merge only editable fields; never overwrite trusted verification fields.
                    val initial = _userProfile.value
                    repositoryScope.launch {
                        try {
                            if (auth?.currentUser?.uid == uid) {
                                val ref = db.document("users/$uid")
                                db.runTransaction { transaction ->
                                    if (!transaction.get(ref).exists()) transaction.set(ref, mapOf(
                                        "name" to initial.name, "village" to initial.village, "district" to initial.district,
                                        "phone" to initial.phone, "state" to initial.state))
                                    Unit
                                }.awaitResult()
                            }
                        }
                        catch (e: Exception) { cloudFailure(e) }
                    }
                }
            }
        }
        listeners += db.document("users/$uid/preferences/settings").addSnapshotListener { snapshot, error ->
            if (auth?.currentUser?.uid == uid) {
                if (error != null) cloudFailure(error)
                else if (snapshot != null && snapshot.exists()) {
                    _selectedLanguage.value = runCatching { AppLanguage.valueOf(snapshot.getString("language").orEmpty()) }.getOrDefault(AppLanguage.ENGLISH)
                    _selectedLocation.value = snapshot.getString("location").orEmpty()
                    _detectedState.value = snapshot.getString("state").orEmpty()
                    _userCoordinates.value = Pair(snapshot.getDouble("latitude") ?: 32.1109, snapshot.getDouble("longitude") ?: 76.5363)
                }
            }
        }
        fun <T : Any> watch(path: String, type: Class<T>, state: MutableStateFlow<List<T>>) {
            listeners += cloud.listen(path, type, { if (auth?.currentUser?.uid == uid) cloudFailure(it) }) {
                if (auth?.currentUser?.uid == uid) state.value = it
            }
        }
        watch("users/$uid/applications", SubmittedApplication::class.java, _submittedApplications)
        watch("users/$uid/grievances", PanchayatGrievance::class.java, _grievances)
        watch("users/$uid/produce", ProduceListing::class.java, _produceListings)
        watch("users/$uid/deliveries", DeliveryRequest::class.java, _deliveryRequests)
        watch("users/$uid/bookings", EquipmentBooking::class.java, _equipmentBookings)
        watch("notices", VillageNotice::class.java, _notices)
        watch("buyers", ProduceBuyer::class.java, _produceBuyers)
        watch("deliveryPartners", DeliveryPartner::class.java, _deliveryPartners)
        watch("equipment", FarmEquipment::class.java, _farmEquipmentList)
        watch("marketPrices", MandiPrice::class.java, _mandiPrices)
        listeners += db.collection("users/$uid/bookmarks").addSnapshotListener { snapshot, error ->
            if (auth?.currentUser?.uid == uid) {
                if (error != null) cloudFailure(error)
                else if (snapshot != null) {
                    val ids = snapshot.documents.map { it.id }.toSet()
                    _schemes.value = _schemes.value.map { it.copy(isBookmarked = it.id in ids) }
                }
            }
        }
    }

    private fun savePreferences() {
        if (auth?.currentUser == null) return
        val uid = requireUid()
        val settings = mapOf("language" to _selectedLanguage.value.name, "location" to _selectedLocation.value,
            "latitude" to _userCoordinates.value.first, "longitude" to _userCoordinates.value.second, "state" to _detectedState.value)
        runMutation { requireNotNull(database).document("users/$uid/preferences/settings").set(settings).awaitResult() }
    }

    private fun requireUid(): String = auth?.currentUser?.uid ?: error("Please sign in first.")

    private suspend fun <T : Any> saveRecord(collection: String, id: String, value: T, type: Class<T>) {
        val uid = requireUid()
        requireNotNull(store) { "Firebase is not configured." }.save("users/$uid/$collection/$id", value, type)
        check(auth?.currentUser?.uid == uid) { "Session changed. Please sign in again." }
    }

    private suspend fun saveProfile(profile: UserProfile) {
        val uid = requireUid()
        check(profile.firebaseUid == uid) { "Session changed." }
        val fields = mapOf("name" to profile.name, "village" to profile.village, "district" to profile.district,
            "phone" to profile.phone, "state" to profile.state)
        requireNotNull(database).document("users/$uid").set(fields, com.google.firebase.firestore.SetOptions.merge()).awaitResult()
        if (auth?.currentUser?.uid == uid) _cloudMessage.value = "Profile saved to Firestore."
    }

    private fun runMutation(action: suspend () -> Unit) {
        val uid = auth?.currentUser?.uid
        repositoryScope.launch {
            mutationLock.withLock {
                if (uid == null || auth?.currentUser?.uid != uid) return@withLock
                _cloudBusy.value = true
                try { action(); if (auth?.currentUser?.uid == uid) _cloudMessage.value = "Saved to Firestore." }
                catch (e: CancellationException) { throw e }
                catch (e: Exception) { if (auth?.currentUser?.uid == uid) cloudFailure(e) }
                finally { _cloudBusy.value = false }
            }
        }
    }

    private fun cloudFailure(error: Exception) {
        _cloudMessage.value = error.localizedMessage ?: "Cloud operation failed. Please retry."
        _supabaseSyncStatus.value = SupabaseSyncStatus(false, false, _cloudMessage.value.orEmpty(), "users/{uid}", 0)
    }

    fun clearCloudMessage() { _cloudMessage.value = null }

    fun close() {
        auth?.removeAuthStateListener(authListener)
        listeners.forEach { it.remove() }
        repositoryScope.cancel()
    }

    init {
        auth?.addAuthStateListener(authListener)
        if (auth == null) _authError.value = "Firebase setup required. Follow FIREBASE_SETUP.md."
        refreshAllRealTimeData()
    }

    fun refreshAllRealTimeData() {
        val (lat, lon) = _userCoordinates.value
        refreshWeather(lat, lon)
        refreshMandiPrices(_detectedState.value)
    }

    fun applyLocation(
        latitude: Double,
        longitude: Double,
        formattedName: String,
        stateName: String,
        isLiveGps: Boolean = true
    ) {
        _selectedLocation.value = formattedName
        _userCoordinates.value = Pair(latitude, longitude)
        _detectedState.value = stateName
        _isGpsActive.value = isLiveGps
        savePreferences()
        refreshWeather(latitude, longitude)
        refreshMandiPrices(stateName)
    }

    fun refreshWeather(latitude: Double = _userCoordinates.value.first, longitude: Double = _userCoordinates.value.second) {
        repositoryScope.launch {
            _isWeatherLoading.value = true
            try {
                val response = RetrofitClient.weatherApiService.getWeatherForecast(
                    latitude = latitude,
                    longitude = longitude
                )

                val current = requireNotNull(response.current) { "Weather readings unavailable" }
                val daily = response.daily

                val temp = requireNotNull(current.temperature) { "Temperature unavailable" }
                val feels = current?.apparentTemperature ?: temp
                val humidity = current?.relativeHumidity ?: 60
                val wind = current?.windSpeed ?: 10.0
                val wCode = current?.weatherCode ?: 1
                val precip = current?.precipitation ?: 0.0

                val maxT = daily?.temperatureMax?.firstOrNull() ?: (temp + 4.0)
                val minT = daily?.temperatureMin?.firstOrNull() ?: (temp - 6.0)
                val rainProb = daily?.precipitationProbabilityMax?.firstOrNull() ?: if (precip > 0.1) 80 else 15

                val (emoji, condEn, condHi, condMr, adviceEn, adviceHi, adviceMr) = getInterpretation(wCode, temp, rainProb, wind)

                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timeStr = "Live • ${timeFormat.format(Date())}"

                _weatherData.value = WeatherData(
                    locationName = _selectedLocation.value,
                    temperature = temp,
                    feelsLike = feels,
                    humidity = humidity,
                    windSpeed = wind,
                    weatherCode = wCode,
                    conditionEn = condEn,
                    conditionHi = condHi,
                    conditionMr = condMr,
                    iconEmoji = emoji,
                    maxTemp = maxT,
                    minTemp = minT,
                    rainProbability = rainProb,
                    farmingAdviceEn = adviceEn,
                    farmingAdviceHi = adviceHi,
                    farmingAdviceMr = adviceMr,
                    lastUpdatedTime = timeStr,
                    isLiveApi = true
                )
            } catch (e: Exception) {
                // Keep the previous reading but explicitly mark it stale.
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timeStr = "Live • ${timeFormat.format(Date())}"
                _weatherData.value = _weatherData.value.copy(
                    locationName = _selectedLocation.value,
                    lastUpdatedTime = "Weather unavailable — previous/sample reading",
                    isLiveApi = false
                )
            } finally {
                _isWeatherLoading.value = false
            }
        }
    }

    fun refreshMandiPrices(preferredState: String = _detectedState.value) {
        // Published market quotes stream from Firestore; never randomize production prices.
        _mandiPrices.value = _mandiPrices.value.sortedByDescending { it.stateEn == preferredState }
    }

    private fun getInterpretation(
        code: Int,
        temp: Double,
        rainProb: Int,
        wind: Double
    ): WeatherInterpretation {
        return when (code) {
            0 -> WeatherInterpretation(
                emoji = "☀️",
                conditionEn = "Clear Sky",
                conditionHi = "साफ आसमान (धूप)",
                conditionMr = "निरभ्र आकाश (सूर्यप्रकाश)",
                adviceEn = if (temp > 35.0) "High heat: Irrigate fields during early morning or late evening." else "Ideal conditions for harvesting and drying grains.",
                adviceHi = if (temp > 35.0) "अधिक गर्मी: फसलों को लू से बचाने के लिए सुबह या शाम को हल्की सिंचाई करें।" else "फसल कटाई और अनाज सुखाने के लिए सर्वोत्तम मौसम।",
                adviceMr = if (temp > 35.0) "जास्त उष्णता: सकाळी किंवा संध्याकाळी सिंचन करा." else "पिकांची कापणी आणि धान्य वाळवण्यासाठी उत्तम हवामान."
            )
            1, 2 -> WeatherInterpretation(
                emoji = "🌤️",
                conditionEn = "Partly Cloudy",
                conditionHi = "धूप व आंशिक बादल",
                conditionMr = "अंशतः ढगाळ",
                adviceEn = "Favorable weather for fertilizer application, sowing and intercultural operations.",
                adviceHi = "उर्वरक प्रयोग, बुआई और निराई-गुड़ाई के लिए अनुकूल मौसम।",
                adviceMr = "खत व्यवस्थापन आणि आंतरमशागतीसाठी अनुकूल हवामान."
            )
            3 -> WeatherInterpretation(
                emoji = "☁️",
                conditionEn = "Overcast Cloudy",
                conditionHi = "घने बादल",
                conditionMr = "ढगाळ वातावरण",
                adviceEn = "Monitor crops for pest outbreaks due to humidity. Delay pesticide spraying.",
                adviceHi = "नमी के कारण कीटों के प्रकोप पर निगरानी रखें। कीटनाशक छिड़काव रोकें।",
                adviceMr = "कीड-रोगांच्या प्रादुर्भावावर लक्ष ठेवा. फवारणी पुढे ढकला."
            )
            45, 48 -> WeatherInterpretation(
                emoji = "🌫️",
                conditionEn = "Foggy",
                conditionHi = "कोहरा / धुंध",
                conditionMr = "धुके",
                adviceEn = "High humidity: Watch for fungal diseases in vegetables and pulses.",
                adviceHi = "सब्जियों और दलहन में फफूंद जनित रोगों से बचाव के लिए सतर्क रहें।",
                adviceMr = "भाजीपाला आणि कडधान्यांमध्ये बुरशीजन्य रोगांपासून संरक्षण करा."
            )
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> WeatherInterpretation(
                emoji = "🌧️",
                conditionEn = "Rain Showers",
                conditionHi = "बारिश / फुहारें",
                conditionMr = "पाऊस / रिमझिम",
                adviceEn = "Avoid chemical spraying. Ensure clean drainage in waterlogged fields.",
                adviceHi = "रासायनिक छिड़काव तुरंत रोकें। जलभराव वाले खेतों में जल निकासी सुनिश्चित करें।",
                adviceMr = "फवारणी थांबवा. शेतात पाणी साचणार नाही याची काळजी घ्या."
            )
            71, 73, 75, 77, 85, 86 -> WeatherInterpretation(
                emoji = "❄️",
                conditionEn = "Cold / Snow",
                conditionHi = "शीतलहर / ठंड",
                conditionMr = "थंडीची लाट",
                adviceEn = "Protect young saplings and horticulture crops from frost.",
                adviceHi = "पाला और पाले से छोटे पौधों व फलदार वृक्षों को ढंककर बचाएं।",
                adviceMr = "कोवळ्या रोपांना आणि फळझाडांना थंडीपासून वाचवा."
            )
            95, 96, 99 -> WeatherInterpretation(
                emoji = "⛈️",
                conditionEn = "Thunderstorm",
                conditionHi = "गरज के साथ आंधी-तूफान",
                conditionMr = "वादळी पाऊस व वीज",
                adviceEn = "Stay in safe shelter. Keep livestock inside and secure farm equipment.",
                adviceHi = "सुरक्षित स्थान पर रहें। पशुओं को बाड़े में सुरक्षित रखें और कृषि उपकरण बचाएं।",
                adviceMr = "सुरक्षित ठिकाणी राहा. जनावरांना गोठ्यात बांधा."
            )
            else -> WeatherInterpretation(
                emoji = "⛅",
                conditionEn = "Moderate Weather",
                conditionHi = "सामान्य मौसम",
                conditionMr = "सामान्य हवामान",
                adviceEn = "Standard agricultural practices recommended.",
                adviceHi = "सामान्य कृषि कार्य जारी रखें।",
                adviceMr = "नेहमीची शेतीकामे सुरू ठेवा."
            )
        }
    }

    private data class WeatherInterpretation(
        val emoji: String,
        val conditionEn: String,
        val conditionHi: String,
        val conditionMr: String,
        val adviceEn: String,
        val adviceHi: String,
        val adviceMr: String
    )

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
        savePreferences()
    }

    fun navigateTo(destination: NavDestination) {
        if (_isAuthenticated.value) _currentDestination.value = destination
    }

    fun setLocation(location: String) {
        _selectedLocation.value = location
        savePreferences()
    }

    fun toggleSchemeBookmark(schemeId: String) = runMutation {
        val uid = requireUid()
        val selected = _schemes.value.first { it.id == schemeId }
        val ref = requireNotNull(database).document("users/$uid/bookmarks/$schemeId")
        if (selected.isBookmarked) ref.delete().awaitResult()
        else ref.set(mapOf("schemeId" to schemeId)).awaitResult()
    }

    suspend fun submitSchemeApplication(scheme: Scheme, applicantName: String, phone: String, aadhaar: String): SubmittedApplication {
        val randomNum = UUID.randomUUID().toString()
        val appId = "APP-$randomNum"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val newApp = SubmittedApplication(
            id = appId,
            schemeTitle = scheme.titleEn,
            schemeTitleHi = scheme.titleHi,
            appId = appId,
            appliedDate = today,
            status = "Saved — submit through official portal",
            progressPercent = 0f,
            applicantName = applicantName.trim(),
            phone = phone.trim(),
            aadhaarLastFour = aadhaar.filter { it.isDigit() }.takeLast(4)
        )

        require(applicantName.isNotBlank() && phone.length in 7..30) { "Enter your name and phone number." }
        saveRecord("applications", newApp.id, newApp, SubmittedApplication::class.java)
        _submittedApplications.value = listOf(newApp) + _submittedApplications.value.filterNot { it.id == newApp.id }
        return newApp
    }

    suspend fun addVillageNotice(title: String, description: String, authority: String, category: String) {
        val newNotice = VillageNotice(
            id = "notice_${UUID.randomUUID()}",
            titleEn = title,
            titleHi = title,
            titleMr = title,
            descriptionEn = description,
            descriptionHi = description,
            descriptionMr = description,
            authorityEn = authority,
            authorityHi = authority,
            authorityMr = authority,
            timestampEn = "Just now",
            timestampHi = "अभी-अभी",
            timestampMr = "आत्ताच",
            iconType = when (category.lowercase()) {
                "water" -> "water"
                "health" -> "health"
                "meeting" -> "meeting"
                else -> "alert"
            }
        )
        require(title.isNotBlank() && description.isNotBlank()) { "Enter a title and notice details." }
        requireNotNull(store) { "Firebase not configured" }.save("notices/${newNotice.id}", newNotice, VillageNotice::class.java)
        _notices.value = listOf(newNotice) + _notices.value.filterNot { it.id == newNotice.id }
    }

    suspend fun submitGrievance(category: String, subject: String, description: String): PanchayatGrievance {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val grievance = PanchayatGrievance(
            id = "GRV-${UUID.randomUUID()}",
            category = category,
            subject = subject,
            description = description,
            applicantName = _userProfile.value.name,
            phone = _userProfile.value.phone,
            date = dateFormat.format(Date())
        )
        require(subject.isNotBlank() && description.isNotBlank()) { "Enter a subject and description." }
        saveRecord("grievances", grievance.id, grievance, PanchayatGrievance::class.java)
        _grievances.value = listOf(grievance) + _grievances.value.filterNot { it.id == grievance.id }
        return grievance
    }

    suspend fun updateProfile(name: String, village: String, district: String, phone: String) {
        require(name.trim().length in 1..100 && village.length <= 120 && district.length <= 120 && phone.length <= 30) { "Check your profile details." }
        saveProfile(_userProfile.value.copy(name = name.trim(), village = village.trim(), district = district.trim(), phone = phone.trim()))
    }

    // Retained names preserve the existing profile component API.
    fun syncWithSupabase(profile: UserProfile = _userProfile.value) {
        repositoryScope.launch {
            try { saveProfile(profile) }
            catch (e: Exception) { cloudFailure(e) }
        }
    }

    fun loadUserDataFromSupabase() = syncWithSupabase()

    private fun authenticate(onSuccess: () -> Unit, onError: (String) -> Unit,
                             action: suspend (FirebaseAuth) -> Unit) {
        if (_isAuthLoading.value) return
        repositoryScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                action(auth ?: error("Firebase is not configured. Follow FIREBASE_SETUP.md."))
                onSuccess()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                val message = e.localizedMessage ?: "Authentication failed. Please try again."
                _authError.value = message
                onError(message)
            } finally { _isAuthLoading.value = false }
        }
    }

    fun loginWithCredentials(usernameOrEmail: String, password: String,
                             onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) =
        authenticate(onSuccess, onError) { firebase ->
            require(android.util.Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail.trim()).matches()) {
                "Enter a valid email address."
            }
            require(password.isNotEmpty()) { "Enter your password." }
            firebase.signInWithEmailAndPassword(usernameOrEmail.trim(), password).awaitResult()
        }

    fun signUpWithCredentials(name: String, email: String, password: String, village: String,
                              district: String, phone: String,
                              onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) =
        authenticate(onSuccess, onError) { firebase ->
            require(name.trim().length in 1..100) { "Enter your name (up to 100 characters)." }
            require(android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) { "Enter a valid email." }
            require(password.length >= 8) { "Use at least 8 characters for your password." }
            require(village.length <= 120 && district.length <= 120 && phone.length <= 30) { "Profile fields are too long." }
            val user = firebase.createUserWithEmailAndPassword(email.trim(), password).awaitResult().user!!
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()).awaitResult()
            try {
                saveProfile(UserProfile(name = name.trim(), email = user.email.orEmpty(), village = village.trim(),
                    district = district.trim(), phone = phone.trim(), firebaseUid = user.uid, authProvider = "password"))
            } catch (e: Exception) {
                cloudFailure(IllegalStateException("Account created, but profile could not be saved. Retry from Profile.", e))
            }
        }

    fun loginWithGoogle(context: Context, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) =
        authenticate(onSuccess, onError) { it.signInWithGoogle(context) }

    fun resetPassword(email: String) = authenticate({}, {}) { firebase ->
        require(android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) { "Enter your email address first." }
        firebase.sendPasswordResetEmail(email.trim()).awaitResult()
        _authError.value = "If this email has an account, a password reset link has been sent."
    }

    fun loginAsDemo() = authenticate({}, {}) { it.signInAnonymously().awaitResult() }

    fun logout() {
        auth?.signOut()
        clearSession()
    }

    // ==========================================
    // FARM PRODUCE & DELIVERY MANAGEMENT
    // ==========================================

    suspend fun addProduceListing(
        cropName: String,
        category: String,
        quantity: Double,
        unit: String,
        expectedPricePerUnit: Double,
        qualityGrade: String,
        harvestDate: String,
        pickupLocation: String,
        preferredBuyerOrMandi: String,
        notes: String,
        productPhoto: String = "🌾"
    ): ProduceListing {
        val timeFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val newListing = ProduceListing(
            id = "PROD-${UUID.randomUUID()}",
            cropName = cropName.trim(),
            category = category,
            quantity = quantity,
            unit = unit,
            expectedPricePerUnit = expectedPricePerUnit,
            qualityGrade = qualityGrade,
            harvestDate = harvestDate,
            pickupLocation = if (pickupLocation.isNotBlank()) pickupLocation else _selectedLocation.value,
            preferredBuyerOrMandi = preferredBuyerOrMandi,
            productPhoto = productPhoto,
            additionalNotes = notes,
            listedDate = timeFormat.format(Date()),
            status = ProduceSaleStatus.ACTIVE_LISTING
        )
        require(cropName.isNotBlank() && quantity.isFinite() && quantity > 0 && expectedPricePerUnit.isFinite() && expectedPricePerUnit > 0) { "Enter a crop, positive quantity and price." }
        saveRecord("produce", newListing.id, newListing, ProduceListing::class.java)
        _produceListings.value = listOf(newListing) + _produceListings.value.filterNot { it.id == newListing.id }
        return newListing
    }

    suspend fun selectBuyerForProduce(produceId: String, buyer: ProduceBuyer) {
        val listing = _produceListings.value.first { it.id == produceId }
        val updated = listing.copy(selectedBuyer = buyer, status = ProduceSaleStatus.BUYER_SELECTED)
        saveRecord("produce", produceId, updated, ProduceListing::class.java)
        _produceListings.value = _produceListings.value.map { if (it.id == produceId) updated else it }
    }

    suspend fun requestDelivery(
        produceId: String,
        deliveryPartner: DeliveryPartner?,
        isSelfTransport: Boolean,
        pickupLocation: String,
        destination: String,
        pickupDate: String,
        pickupTime: String,
        vehicleType: String,
        instructions: String,
        estimatedFare: Int
    ): DeliveryRequest {
        val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val nowStr = timeFormat.format(Date())

        val targetListing = _produceListings.value.find { it.id == produceId }
        val cropTitle = targetListing?.cropName ?: "Agricultural Produce"
        val qty = "${targetListing?.quantity ?: 0.0} ${targetListing?.unit ?: "Qtl"}"

        val newRequest = DeliveryRequest(
            id = "DEL-${UUID.randomUUID()}",
            produceListingId = produceId,
            produceTitle = cropTitle,
            quantity = qty,
            pickupLocation = if (pickupLocation.isNotBlank()) pickupLocation else _selectedLocation.value,
            destination = destination,
            pickupDate = pickupDate,
            pickupTime = pickupTime,
            vehicleType = vehicleType,
            deliveryPartner = if (isSelfTransport) null else deliveryPartner,
            isSelfTransport = isSelfTransport,
            estimatedFare = estimatedFare,
            additionalInstructions = instructions,
            status = DeliveryStatus.REQUESTED,
            statusUpdatedAt = nowStr,
            trackingUpdates = listOf(
                "Delivery Requested" to nowStr,
                "Awaiting Driver Confirmation" to "Just now"
            )
        )

        require(targetListing != null && pickupDate.isNotBlank() && destination.isNotBlank()) { "Select a listing, date and destination." }
        val uid = requireUid()
        val cloud = requireNotNull(store)
        val updatedListing = targetListing.copy(deliveryRequest = newRequest, status = ProduceSaleStatus.DELIVERY_REQUESTED)
        val db = requireNotNull(database)
        db.runBatch { batch ->
            batch.set(db.document("users/$uid/deliveries/${newRequest.id}"), cloud.encode(newRequest, DeliveryRequest::class.java))
            batch.set(db.document("users/$uid/produce/$produceId"), cloud.encode(updatedListing, ProduceListing::class.java))
        }.awaitResult()
        check(auth?.currentUser?.uid == uid) { "Session changed." }
        _deliveryRequests.value = listOf(newRequest) + _deliveryRequests.value.filterNot { it.id == newRequest.id }

        _produceListings.value = _produceListings.value.map { listing ->
            if (listing.id == produceId) {
                listing.copy(
                    deliveryRequest = newRequest,
                    status = ProduceSaleStatus.DELIVERY_REQUESTED
                )
            } else {
                listing
            }
        }

        return newRequest
    }

    fun advanceDeliveryStatus(requestId: String) = runMutation {
        val request = _deliveryRequests.value.first { it.id == requestId }
        val next = DeliveryStatus.entries.getOrNull(request.status.ordinal + 1)
            ?: error("This delivery is already complete.")
        com.google.firebase.functions.FirebaseFunctions.getInstance("asia-south1")
            .getHttpsCallable("updateDeliveryStatus")
            .call(mapOf("uid" to requireUid(), "deliveryId" to requestId, "status" to next.name))
            .awaitResult()
    }

    suspend fun deleteProduceListing(produceId: String) {
        val uid = requireUid()
        requireNotNull(store).delete("users/$uid/produce/$produceId")
        if (auth?.currentUser?.uid == uid) _produceListings.value = _produceListings.value.filterNot { it.id == produceId }
    }

    // ==========================================
    // FARM EQUIPMENT BOOKING MANAGEMENT
    // ==========================================

    suspend fun bookEquipment(
        equipmentId: String,
        bookingDate: String,
        timeSlot: String,
        durationOrArea: String,
        farmLocation: String,
        instructions: String,
        cost: Int
    ): EquipmentBooking {
        val timeFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val equipment = _farmEquipmentList.value.find { it.id == equipmentId }

        val newBooking = EquipmentBooking(
            id = "BKG-${UUID.randomUUID()}",
            equipmentId = equipmentId,
            equipmentName = equipment?.nameEn ?: "Farm Machinery",
            providerName = equipment?.providerName ?: "Local CHC Provider",
            bookingDate = bookingDate,
            timeSlot = timeSlot,
            durationOrArea = durationOrArea,
            farmLocation = if (farmLocation.isNotBlank()) farmLocation else _selectedLocation.value,
            instructions = instructions,
            estimatedCost = cost,
            status = EquipmentBookingStatus.REQUESTED,
            statusDate = timeFormat.format(Date()),
            providerPhone = "",
            iconEmoji = equipment?.iconEmoji ?: "🚜"
        )

        require(equipment != null && bookingDate.isNotBlank() && farmLocation.isNotBlank() && cost >= 0) { "Choose equipment, a date and farm location." }
        saveRecord("bookings", newBooking.id, newBooking, EquipmentBooking::class.java)
        _equipmentBookings.value = listOf(newBooking) + _equipmentBookings.value.filterNot { it.id == newBooking.id }
        return newBooking
    }

    suspend fun cancelEquipmentBooking(bookingId: String) {
        val updated = _equipmentBookings.value.first { it.id == bookingId }.copy(status = EquipmentBookingStatus.CANCELLED)
        saveRecord("bookings", bookingId, updated, EquipmentBooking::class.java)
        _equipmentBookings.value = _equipmentBookings.value.map { if (it.id == bookingId) updated else it }
    }

    companion object {
        val defaultSchemes = listOf(
            Scheme(
                id = "pm_tractor",
                titleEn = "PM Tractor Subsidy Yojana",
                titleHi = "प्रधानमंत्री ट्रैक्टर सब्सिडी योजना",
                titleMr = "पंतप्रधान ट्रॅक्टर अनुदान योजना",
                category = SchemeCategory.FARMERS,
                descriptionEn = "Get up to 50% government subsidy (max ₹2,00,000) for purchasing a new agricultural tractor and machinery.",
                descriptionHi = "नया कृषि ट्रैक्टर और उपकरण खरीदने के लिए 50% तक सरकारी सब्सिडी (अधिकतम ₹2,00,000) प्राप्त करें।",
                descriptionMr = "नवीन कृषी ट्रॅक्टर व उपकरणे खरेदीसाठी ५०% पर्यंत सरकारी अनुदान (कमाल ₹२,००,०००) मिळवा.",
                benefitEn = "50% Financial Subsidy up to ₹2,00,000 direct in bank",
                benefitHi = "₹2,00,000 तक 50% वित्तीय सहायता सीधे बैंक खाते में",
                benefitMr = "५०% आर्थिक अनुदान ₹२,००,००० थेट बँक खात्यात",
                eligibilityEn = listOf(
                    "Applicant farmer must own cultivable agricultural land (minimum 1 acre)",
                    "Must not have received tractor subsidy in the past 7 years",
                    "Only one person per family can avail the scheme",
                    "Bank account linked with Aadhaar is mandatory"
                ),
                eligibilityHi = listOf(
                    "आवेदक किसान के नाम पर न्यूनतम 1 एकड़ कृषि योग्य भूमि होनी चाहिए",
                    "पिछले 7 वर्षों में ट्रैक्टर सब्सिडी प्राप्त नहीं की होनी चाहिए",
                    "परिवार का केवल एक सदस्य ही आवेदन कर सकता है",
                    "आधार से जुड़ा बैंक खाता अनिवार्य है"
                ),
                eligibilityMr = listOf(
                    "शेतकऱ्याकडे किमान १ एकर शेतजमीन असावी",
                    "मागील ७ वर्षात ट्रॅक्टर अनुदानाचा लाभ घेतलेला नसावा",
                    "एका कुटुंबातून एकच व्यक्ती पात्र",
                    "आधार संलग्न बँक खाते आवश्यक"
                ),
                documentsEn = listOf("Aadhaar Card", "Land 7/12 & 8A / Khasra Khatauni", "Bank Passbook Copy", "Passport Photo", "Income Certificate"),
                documentsHi = listOf("आधार कार्ड", "खसरा खतौनी / 7/12 पर्चा", "बैंक पासबुक प्रति", "पासपोर्ट साइज फोटो", "आय प्रमाण पत्र"),
                documentsMr = listOf("आधार कार्ड", "७/१२ व ८-अ उतारा", "बँक पासबुक", "पासपोर्ट फोटो", "उत्पन्नाचा दाखला"),
                applicationProcessEn = "Fill the digital form, upload certified land documents, and submit to the nearest Block Agricultural Officer or online through this portal.",
                applicationProcessHi = "डिजिटल फॉर्म भरें, प्रमाणित भूमि दस्तावेज संलग्न करें और इस पोर्टल या ब्लॉक कृषि अधिकारी के पास जमा करें।",
                officialLink = "https://agrimachinery.nic.in",
                iconType = "tractor"
            ),
            Scheme(
                id = "pmksy_irrigation",
                titleEn = "Pradhan Mantri Krishi Sinchayee Yojana (PMKSY)",
                titleHi = "प्रधानमंत्री कृषि सिंचाई योजना (PMKSY)",
                titleMr = "पंतप्रधान कृषी सिंचन योजना",
                category = SchemeCategory.FARMERS,
                descriptionEn = "Subsidies up to 55% for small farmers to install drip and sprinkler micro-irrigation systems to conserve water.",
                descriptionHi = "जल संरक्षण हेतु ड्रिप और स्प्रिंकलर सूक्ष्म सिंचाई उपकरण स्थापित करने के लिए छोटे किसानों को 55% तक सब्सिडी।",
                descriptionMr = "पाणी बचतीसाठी ठिबक व तुषार सिंचन पद्धती बसवण्यासाठी लहान शेतकऱ्यांना ५५% पर्यंत अनुदान.",
                benefitEn = "45% - 55% Subsidy on Micro-irrigation equipment",
                benefitHi = "सूक्ष्म सिंचाई उपकरणों पर 45% से 55% सब्सिडी",
                benefitMr = "सूक्ष्म सिंचन साधनांवर ४५% - ५५% अनुदान",
                eligibilityEn = listOf(
                    "All farmers with assured water source on their farm",
                    "Small and marginal farmers receive up to 55% subsidy",
                    "Valid electricity/solar pump connection or water storage tank"
                ),
                eligibilityHi = listOf(
                    "खेत में सुनिश्चित जल स्रोत वाले सभी किसान",
                    "छोटे और सीमांत किसानों को 55% तक अनुदान",
                    "वैध बोरवेल/कुआं या जल संचय टैंक"
                ),
                eligibilityMr = listOf(
                    "शेतात पाण्याचा निश्चित स्त्रोत असलेले सर्व शेतकरी",
                    "अल्प व अत्यल्प भूधारक शेतकऱ्यांना ५५% अनुदान"
                ),
                documentsEn = listOf("Aadhaar Card", "Land Records (7/12)", "Water Source Certificate", "Bank Account Details"),
                documentsHi = listOf("आधार कार्ड", "जमीन के कागजात", "जल स्रोत प्रमाण पत्र", "बैंक खाता विवरण"),
                documentsMr = listOf("आधार कार्ड", "जमिनीचे उतारे", "पाण्याचा स्त्रोत पुरावा", "बँक तपशील"),
                applicationProcessEn = "Apply with farm survey number, select authorized micro-irrigation dealer, and get site inspection completed by taluka officer.",
                applicationProcessHi = "खेत के खसरा नंबर के साथ आवेदन करें, अधिकृत डीलर का चयन करें और साइट निरीक्षण करवाएं।",
                officialLink = "https://pmksy.gov.in",
                iconType = "water"
            ),
            Scheme(
                id = "certified_seeds",
                titleEn = "High-Yield Certified Seed Distribution",
                titleHi = "उच्च उपज प्रमाणित बीज वितरण योजना",
                titleMr = "उच्च उत्पादन देणारे प्रमाणित बियाणे वाटप",
                category = SchemeCategory.FARMERS,
                descriptionEn = "Free or 75% subsidized distribution of drought-tolerant, high-yield certified seeds for Kharif & Rabi seasons.",
                descriptionHi = "खरीफ और रबी मौसम के लिए सूखा-सहनशील, उच्च उपज वाले प्रमाणित बीजों का मुफ्त या 75% रियायती वितरण।",
                descriptionMr = "खरीप आणि रब्बी हंगामासाठी दुष्काळ सहन करणाऱ्या उच्च प्रतीच्या बियाणांचे मोफत किंवा ७५% सवलतीवर वाटप.",
                benefitEn = "Free seed mini-kits (Wheat, Paddy, Mustard, Pulses)",
                benefitHi = "मुफ्त बीज मिनी किट (गेहूं, धान, सरसों, दलहन)",
                benefitMr = "मोफत बियाणे मिनी-कीट (गहू, भात, मोहरी, कडधान्ये)",
                eligibilityEn = listOf(
                    "Small and marginal farmers holding Kisan Credit Card",
                    "Priority given to SC/ST and women farmers"
                ),
                eligibilityHi = listOf(
                    "किसान क्रेडिट कार्ड धारक छोटे और सीमांत किसान",
                    "अनुसूचित जाति/जनजाति और महिला किसानों को प्राथमिकता"
                ),
                eligibilityMr = listOf(
                    "किसान क्रेडिट कार्डधारक अल्पभूधारक शेतकरी",
                    "महिला शेतकऱ्यांना प्राधान्य"
                ),
                documentsEn = listOf("Aadhaar Card", "Kisan Credit Card / Land Passbook"),
                documentsHi = listOf("आधार कार्ड", "किसान क्रेडिट कार्ड / खतौनी"),
                documentsMr = listOf("आधार कार्ड", "जमीन कागदपत्रे"),
                applicationProcessEn = "Register at Village Krishi Seva Kendra or Gram Panchayat office before sowing season starts.",
                applicationProcessHi = "बुवाई का मौसम शुरू होने से पहले ग्राम कृषि सेवा केंद्र पर पंजीकरण कराएं।",
                officialLink = "https://seednet.gov.in",
                iconType = "seed"
            ),
            Scheme(
                id = "pm_kisan",
                titleEn = "PM Kisan Samman Nidhi",
                titleHi = "प्रधानमंत्री किसान सम्मान निधि",
                titleMr = "पंतप्रधान किसान सन्मान निधी",
                category = SchemeCategory.FARMERS,
                descriptionEn = "Direct income support of ₹6,000 per year in three equal 4-monthly installments directly into bank accounts.",
                descriptionHi = "बैंक खातों में सीधे तीन समान 4-मासिक किस्तों में प्रति वर्ष ₹6,000 की प्रत्यक्ष आय सहायता।",
                descriptionMr = "शेतकऱ्यांच्या थेट बँक खात्यात दरवर्षी ₹६,००० तीन हप्त्यांमध्ये जमा.",
                benefitEn = "₹6,000/year directly credited in bank",
                benefitHi = "₹6,000 प्रति वर्ष सीधे बैंक खाते में",
                benefitMr = "₹६,००० प्रति वर्ष थेट बँक खात्यात",
                eligibilityEn = listOf("All landholding farmer families with cultivable land in revenue records", "eKYC completion is mandatory"),
                eligibilityHi = listOf("राजस्व अभिलेखों में कृषि योग्य भूमि वाले सभी किसान परिवार", "ई-केवाईसी पूरा होना अनिवार्य है"),
                eligibilityMr = listOf("शेतजमीन असलेले सर्व शेतकरी कुटुंब", "ई-केवायसी अनिवार्य"),
                documentsEn = listOf("Aadhaar Card", "Land Registration", "Bank Account with NPCI mapping"),
                documentsHi = listOf("आधार कार्ड", "भूमि दस्तावेज", "एनपीसीआई मैप बैंक खाता"),
                documentsMr = listOf("आधार कार्ड", "जमीन मालकी पुरावा", "बँक खाते"),
                applicationProcessEn = "Apply via PM Kisan portal or Village CSC center with Aadhaar OTP verification.",
                applicationProcessHi = "आधार ओटीपी सत्यापन के साथ पीएम किसान पोर्टल या सीएससी से आवेदन करें।",
                officialLink = "https://pmkisan.gov.in",
                iconType = "farmer"
            ),
            Scheme(
                id = "matru_vandana",
                titleEn = "Pradhan Mantri Matru Vandana Yojana (PMMVY)",
                titleHi = "प्रधानमंत्री मातृ वंदना योजना (PMMVY)",
                titleMr = "पंतप्रधान मातृ वंदना योजना",
                category = SchemeCategory.WOMEN,
                descriptionEn = "Maternity cash incentive of ₹5,000 for pregnant women and lactating mothers for health and nutrition.",
                descriptionHi = "गर्भवती महिलाओं और स्तनपान कराने वाली माताओं के स्वास्थ्य और पोषण के लिए ₹5,000 की मातृत्व नकद सहायता।",
                descriptionMr = "गरोदर व स्तनदा मातांच्या आरोग्यासाठी आणि पोषणासाठी ₹५,००० ची मातृत्व आर्थिक मदत.",
                benefitEn = "₹5,000 cash assistance in direct DBT installments",
                benefitHi = "सीधे बैंक खाते में ₹5,000 नकद सहायता",
                benefitMr = "थेट बँक खात्यात ₹५,००० रोख सहाय्य",
                eligibilityEn = listOf("Pregnant Women and Lactating Mothers aged 19 and above", "First living child of the family"),
                eligibilityHi = listOf("19 वर्ष और उससे अधिक आयु की गर्भवती महिलाएं", "परिवार का पहला जीवित बच्चा"),
                eligibilityMr = listOf("१९ वर्षे किंवा त्यावरील गरोदर महिला"),
                documentsEn = listOf("Mother-Child Protection (MCP) Card", "Aadhaar Card of Husband and Wife", "Joint Bank Account"),
                documentsHi = listOf("एमसीपी कार्ड", "पति-पत्नी का आधार कार्ड", "बैंक पासबुक"),
                documentsMr = listOf("आरोग्य कार्ड", "पती-पत्नीचे आधार कार्ड", "बँक पासबुक"),
                applicationProcessEn = "Register at the local Anganwadi Centre (AWC) or Primary Health Centre (PHC).",
                applicationProcessHi = "निकटतम आंगनवाड़ी केंद्र या प्राथमिक स्वास्थ्य केंद्र पर पंजीकरण करें।",
                officialLink = "https://wcd.nic.in",
                iconType = "woman"
            ),
            Scheme(
                id = "post_matric_scholarship",
                titleEn = "National Pre & Post-Matric Scholarship",
                titleHi = "राष्ट्रीय प्री व पोस्ट-मैट्रिक छात्रवृत्ति",
                titleMr = "राष्ट्रीय पूर्व व उच्च माध्यमिक शिष्यवृत्ती",
                category = SchemeCategory.STUDENTS,
                descriptionEn = "Full tuition fee support and maintenance allowance for rural school and college students.",
                descriptionHi = "ग्रामीण स्कूली और कॉलेज के छात्रों के लिए पूर्ण ट्यूशन शुल्क सहायता और भरण-पोषण भत्ता।",
                descriptionMr = "ग्रामीण भागातील शालेय व महाविद्यालयीन विद्यार्थ्यांसाठी पूर्ण फी माफी व निर्वाह भत्ता.",
                benefitEn = "100% Tuition Fee Reimbursement + ₹1,200/mo allowance",
                benefitHi = "100% ट्यूशन फीस वापसी + ₹1,200/माह भत्ता",
                benefitMr = "१००% फी परतावा + ₹१,२०० मासिक भत्ता",
                eligibilityEn = listOf("Students studying in recognized class 9-12 or Degree/Diploma courses", "Family income below ₹2.5 Lakh/year"),
                eligibilityHi = listOf("मान्यता प्राप्त कक्षा 9-12 या डिग्री/डिप्लोमा में अध्ययनरत छात्र", "पारिवारिक आय ₹2.5 लाख/वर्ष से कम"),
                eligibilityMr = listOf("इयत्ता ९ वी ते पदवीचे विद्यार्थी", "वार्षिक उत्पन्न २.५ लाखांपेक्षा कमी"),
                documentsEn = listOf("Aadhaar Card", "Current Year Fee Receipt & Bonafide", "Previous Marks Memo", "Caste & Income Certificate"),
                documentsHi = listOf("आधार कार्ड", "फीस रसीद और बोनाफाइड", "पिछली अंकसूची", "आय और जाति प्रमाण पत्र"),
                documentsMr = listOf("आधार कार्ड", "महाविद्यालय प्रवेश पावती", "गुणपत्रिका", "उत्पन्नाचा दाखला"),
                applicationProcessEn = "Submit online through National Scholarship Portal (NSP) and get verified by school/college.",
                applicationProcessHi = "राष्ट्रीय छात्रवृत्ति पोर्टल (NSP) पर आवेदन करें और स्कूल/कॉलेज से सत्यापित कराएं।",
                officialLink = "https://scholarships.gov.in",
                iconType = "student"
            ),
            Scheme(
                id = "shram_yogi_maandhan",
                titleEn = "Pradhan Mantri Shram Yogi Maandhan (PM-SYM)",
                titleHi = "प्रधानमंत्री श्रम योगी मानधन योजना",
                titleMr = "पंतप्रधान श्रम योगी मानधन योजना",
                category = SchemeCategory.WORKERS,
                descriptionEn = "Assured pension of ₹3,000 per month for unorganized village workers, agricultural laborers and artisans after age 60.",
                descriptionHi = "60 वर्ष की आयु के बाद असंगठित ग्रामीण श्रमिकों, खेतिहर मजदूरों और कारीगरों के लिए ₹3,000 प्रति माह सुनिश्चित पेंशन।",
                descriptionMr = "असंघटित कामगार, शेतमजूर आणि कारागिरांसाठी वयाच्या ६० वर्षानंतर दरमहा ₹३,००० खात्रीशीर पेन्शन.",
                benefitEn = "Assured ₹3,000 monthly pension for life",
                benefitHi = "आजीवन ₹3,000 मासिक पेंशन सुनिश्चित",
                benefitMr = "आयुष्यभर दरमहा ₹३,००० खात्रीशीर पेन्शन",
                eligibilityEn = listOf("Unorganized workers aged 18 to 40 years", "Monthly income ₹15,000 or less", "Not covered under EPFO/NPS"),
                eligibilityHi = listOf("18 से 40 वर्ष के असंगठित श्रमिक", "मासिक आय ₹15,000 या कम", "ईपीएफओ/एनपीएस के तहत नहीं"),
                eligibilityMr = listOf("वय १८ ते ४० वर्षे कामगार", "मासिक उत्पन्न ₹१५,००० पेक्षा कमी"),
                documentsEn = listOf("Aadhaar Card", "Savings Bank Account / Jan Dhan Account with Auto-debit"),
                documentsHi = listOf("आधार कार्ड", "जन धन / बचत बैंक खाता विवरण"),
                documentsMr = listOf("आधार कार्ड", "बँक खाते"),
                applicationProcessEn = "Enroll through Common Service Centre (CSC) with Aadhaar and bank details in 5 minutes.",
                applicationProcessHi = "आधार और बैंक खाते के साथ सीएससी केंद्र पर 5 मिनट में पंजीकरण कराएं।",
                officialLink = "https://maandhan.in",
                iconType = "worker"
            ),
            Scheme(
                id = "old_age_pension",
                titleEn = "Indira Gandhi National Old Age Pension (IGNOAPS)",
                titleHi = "इंदिरा गांधी राष्ट्रीय वृद्धावस्था पेंशन",
                titleMr = "इंदिरा गांधी राष्ट्रीय वृद्धापकाळ निवृत्तीवेतन",
                category = SchemeCategory.SENIOR_CITIZENS,
                descriptionEn = "Monthly pension support for village senior citizens living below the poverty line to ensure dignified living.",
                descriptionHi = "गरीबी रेखा से नीचे रहने वाले ग्रामीण वरिष्ठ नागरिकों के लिए सम्मानजनक जीवन सुनिश्चित करने हेतु मासिक पेंशन सहायता।",
                descriptionMr = "ग्रामीण भागातील ज्येष्ठ नागरिकांसाठी सन्मानपूर्वक जगण्यासाठी दरमहा आर्थिक पेन्शन.",
                benefitEn = "Monthly pension directly deposited in bank/post office",
                benefitHi = "मासिक पेंशन सीधे बैंक या डाकघर खाते में जमा",
                benefitMr = "दरमहा पेन्शन थेट बँक अथवा पोस्ट खात्यात",
                eligibilityEn = listOf("Senior citizens aged 60 years or above", "Belongs to BPL (Below Poverty Line) household"),
                eligibilityHi = listOf("60 वर्ष या उससे अधिक आयु के वरिष्ठ नागरिक", "बीपीएल राशन कार्ड धारक परिवार"),
                eligibilityMr = listOf("वय ६० वर्षे किंवा जास्त", "दारिद्र्यरेषेखालील (BPL) कुटुंब"),
                documentsEn = listOf("Age Proof / Aadhaar", "BPL Ration Card", "Bank / Post Office Passbook"),
                documentsHi = listOf("आयु प्रमाण / आधार कार्ड", "बीपीएल राशन कार्ड", "बैंक पासबुक"),
                documentsMr = listOf("वय पुरावा / आधार", "बीपीएल शिधापत्रिका", "बँक पासबुक"),
                applicationProcessEn = "Submit verification application at Gram Panchayat or Taluka Social Welfare Office.",
                applicationProcessHi = "ग्राम पंचायत या ब्लॉक समाज कल्याण कार्यालय में सत्यापन आवेदन जमा करें।",
                officialLink = "https://nsap.nic.in",
                iconType = "senior"
            )
        )

        val defaultMandiPrices = listOf(
            MandiPrice("wheat", "Wheat (गेहूं / गहू)", "गेहूं (Wheat)", "गहू (Wheat)", "Nagpur APMC, Nagpur", "नागपुर एपीएमसी", "नागपूर बाजार समिती", 2650, "Qtl", 2450, 2850, 40),
            MandiPrice("soybean", "Soybean (सोयाबीन)", "सोयाबीन (Soybean)", "सोयाबीन (Soybean)", "Kalmeshwar APMC, Nagpur", "कलमेश्वर मंडी", "कळमेश्वर बाजार समिती", 4620, "Qtl", 4300, 4850, 80),
            MandiPrice("cotton", "Cotton (कपास / कापूस)", "कपास (Cotton)", "कापूस (Cotton)", "Hinganghat APMC, Wardha", "हिंगणघाट मंडी", "हिंगणघाट बाजार समिती", 7350, "Qtl", 6900, 7600, -30),
            MandiPrice("paddy", "Paddy / Basmati (धान / भात)", "धान / बासमती", "भात / बासमती", "Dubagga APMC, Lucknow", "दुबग्गा मंडी", "दुबग्गा बाजार समिती", 2320, "Qtl", 2180, 2650, 25),
            MandiPrice("mustard", "Mustard (सरसों / मोहरी)", "सरसों (Mustard)", "मोहरी (Mustard)", "Kanpur Grain Mandi, Kanpur", "कानपुर ग्रेन मंडी", "कानपूर धान्य बाजार", 5500, "Qtl", 5200, 5750, 50),
            MandiPrice("chana", "Chana / Gram (चना / हरभरा)", "चना (Chana)", "हरभरा (Chana)", "Latur APMC, Maharashtra", "लातूर मंडी", "लातूर बाजार समिती", 5850, "Qtl", 5600, 6100, 65)
        )

        val defaultNotices = listOf(
            VillageNotice(
                id = "not_1",
                titleEn = "Water Supply Maintenance",
                titleHi = "जल आपूर्ति पाइपलाइन रखरखाव",
                titleMr = "पाणी पुरवठा पाइपलाइन देखभाल",
                descriptionEn = "Scheduled maintenance for Sector 4 water distribution pipelines tomorrow from 10:00 AM to 2:00 PM.",
                descriptionHi = "कल सुबह 10:00 बजे से दोपहर 2:00 बजे तक सेक्टर 4 की जल वितरण पाइपलाइन की मरम्मत की जाएगी।",
                descriptionMr = "उद्या सकाळी १० ते दुपारी २ या वेळेत सेक्टर ४ मधील पाणी पुरवठा जलवाहिनी दुरुस्तीचे काम राहील.",
                authorityEn = "Gram Panchayat Office",
                authorityHi = "ग्राम पंचायत कार्यालय",
                authorityMr = "ग्रामपंचायत कार्यालय",
                timestampEn = "Today 9:30 AM",
                timestampHi = "आज सुबह 9:30",
                timestampMr = "आज स. ९:३०",
                iconType = "water"
            ),
            VillageNotice(
                id = "not_2",
                titleEn = "Free Health & Eye Checkup Camp",
                titleHi = "निःशुल्क स्वास्थ्य एवं नेत्र जांच शिविर",
                titleMr = "मोफत आरोग्य व नेत्र तपासणी शिबीर",
                descriptionEn = "Free general health checkup and medicine distribution at Panchayat Bhawan this Sunday 9 AM onwards.",
                descriptionHi = "इस रविवार सुबह 9 बजे से पंचायत भवन में निःशुल्क सामान्य स्वास्थ्य जांच और दवा वितरण किया जाएगा।",
                descriptionMr = "या रविवारी सकाळी ९ पासून पंचायत भवनात मोफत आरोग्य तपासणी व औषध वाटप केले जाईल.",
                authorityEn = "Primary Health Centre (PHC)",
                authorityHi = "प्राथमिक स्वास्थ्य केंद्र (PHC)",
                authorityMr = "प्राथमिक आरोग्य केंद्र",
                timestampEn = "Yesterday",
                timestampHi = "कल",
                timestampMr = "काल",
                iconType = "health"
            ),
            VillageNotice(
                id = "not_3",
                titleEn = "Gram Sabha General Meeting",
                titleHi = "ग्राम सभा साधारण मासिक बैठक",
                titleMr = "ग्रामसभा सर्वसाधारण मासिक सभा",
                descriptionEn = "Monthly general assembly scheduled for Friday to discuss local road concrete works and MNREGA job cards.",
                descriptionHi = "स्थानीय सड़क कंक्रीट निर्माण और मनरेगा जॉब कार्ड पर चर्चा के लिए शुक्रवार को ग्राम सभा की मासिक बैठक आयोजित है।",
                descriptionMr = "गावातील रस्ते काँक्रीटीकरण व मनरेगा जॉब कार्ड वाटप विषयावर शुक्रवारी ग्रामसभा आयोजित करण्यात आली आहे.",
                authorityEn = "Sarpanch Smt. Devi",
                authorityHi = "सरपंच श्रीमती देवी",
                authorityMr = "सरपंच श्रीमती देवी",
                timestampEn = "3 Days Ago",
                timestampHi = "3 दिन पहले",
                timestampMr = "३ दिवसांपूर्वी",
                iconType = "meeting"
            )
        )

        val defaultSubmittedApplications = listOf(
            SubmittedApplication(
                id = "app_1",
                schemeTitle = "National Pre & Post-Matric Scholarship",
                schemeTitleHi = "राष्ट्रीय प्री व पोस्ट-मैट्रिक छात्रवृत्ति",
                appId = "APP-352975",
                appliedDate = "2026-08-29",
                status = "In Review",
                progressPercent = 0.55f
            ),
            SubmittedApplication(
                id = "app_2",
                schemeTitle = "Krishi Sinchayee Yojana (Drip Irrigation)",
                schemeTitleHi = "कृषि सिंचाई योजना (ड्रिप इरिगेशन)",
                appId = "APP-AGRI-2026-081",
                appliedDate = "2026-08-15",
                status = "In Review",
                progressPercent = 0.40f
            )
        )

        val defaultEmergencyContacts = listOf(
            EmergencyContact("police", "Police", "पुलिस", "पोलीस", "100", "police", 0xFFD32F2F),
            EmergencyContact("ambulance", "Ambulance", "एम्बुलेंस", "रुग्णवाहिका", "108", "ambulance", 0xFFE91E63),
            EmergencyContact("women", "Women", "महिला हेल्पलाइन", "महिला हेल्पलाइन", "1091", "women", 0xFFF57C00),
            EmergencyContact("kisan", "Kisan Call Center", "किसान कॉल सेंटर", "किसान कॉल सेंटर", "1551", "kisan", 0xFF388E3C),
            EmergencyContact("disaster", "Disaster Helpline", "आपदा राहत", "आपत्ती निवारण", "1070", "disaster", 0xFF616161)
        )

        val defaultCitizenServices = listOf(
            CitizenService(
                id = "aadhaar",
                sectionEn = "Identity & Documents (पहचान और दस्तावेज़)",
                sectionHi = "पहचान और दस्तावेज़",
                sectionMr = "ओळख व कागदपत्रे",
                titleEn = "Aadhaar Card (आधार कार्ड)",
                titleHi = "आधार कार्ड",
                titleMr = "आधार कार्ड",
                descriptionEn = "Download, update address or verify status",
                descriptionHi = "डाउनलोड करें, पता अपडेट करें या स्थिति जांचें",
                descriptionMr = "डाऊनलोड करा, पत्ता अपडेट करा किंवा स्थिती तपासा",
                actionTextEn = "Open DigiLocker Card →",
                actionTextHi = "डिजीलॉकर कार्ड खोलें →",
                actionTextMr = "डिजीलॉकर कार्ड उघडा →",
                iconType = "aadhaar"
            ),
            CitizenService(
                id = "pan",
                sectionEn = "Identity & Documents (पहचान और दस्तावेज़)",
                sectionHi = "पहचान और दस्तावेज़",
                sectionMr = "ओळख व कागदपत्रे",
                titleEn = "PAN Card (पैन कार्ड)",
                titleHi = "पैन कार्ड",
                titleMr = "पॅन कार्ड",
                descriptionEn = "Apply Instant e-PAN or link with Aadhaar",
                descriptionHi = "इंस्टेंट ई-पैन आवेदन करें या आधार से लिंक करें",
                descriptionMr = "इन्स्टंट ई-पॅन अर्ज करा किंवा आधार लिंक करा",
                actionTextEn = "View / Apply →",
                actionTextHi = "देखें / आवेदन करें →",
                actionTextMr = "पहा / अर्ज करा →",
                iconType = "pan"
            ),
            CitizenService(
                id = "ration",
                sectionEn = "Identity & Documents (पहचान और दस्तावेज़)",
                sectionHi = "पहचान और दस्तावेज़",
                sectionMr = "ओळख व कागदपत्रे",
                titleEn = "Ration Card (राशन कार्ड)",
                titleHi = "राशन कार्ड",
                titleMr = "शिधापत्रिका",
                descriptionEn = "NFSA monthly ration quota and member list",
                descriptionHi = "राष्ट्रीय खाद्य सुरक्षा मासिक कोटा और सदस्य सूची",
                descriptionMr = "मासिक रेशन कोटा आणि कुटुंबातील सदस्य यादी",
                actionTextEn = "View Ration Card →",
                actionTextHi = "राशन कार्ड देखें →",
                actionTextMr = "शिधापत्रिका पहा →",
                iconType = "ration"
            ),
            CitizenService(
                id = "electricity",
                sectionEn = "Utilities & Payments (उपयोगिता और बिल)",
                sectionHi = "उपयोगिता और बिल",
                sectionMr = "उपयुक्तता व बिले",
                titleEn = "Electricity Bill (वीज बिल)",
                titleHi = "बिजली बिल",
                titleMr = "वीज बिल",
                descriptionEn = "Check & pay DISCOM state electricity bill",
                descriptionHi = "राज्य विद्युत वितरण कंपनी का बिल देखें और भरें",
                descriptionMr = "महावितरण वीज बिल तपासा आणि भरा",
                actionTextEn = "Pay Bill →",
                actionTextHi = "बिल भरें →",
                actionTextMr = "बिल भरा →",
                iconType = "electric"
            ),
            CitizenService(
                id = "water_bill",
                sectionEn = "Utilities & Payments (उपयोगिता और बिल)",
                sectionHi = "उपयोगिता और बिल",
                sectionMr = "उपयुक्तता व बिले",
                titleEn = "Water Bill (जल बिल)",
                titleHi = "जल बिल",
                titleMr = "पाणी बिल",
                descriptionEn = "Rural & Municipal water connection charges",
                descriptionHi = "ग्रामीण एवं नगर परिषद जल कनेक्शन शुल्क भुगतान",
                descriptionMr = "ग्रामीण व नगर परिषद पाणीपट्टी शुल्क",
                actionTextEn = "Pay Bill →",
                actionTextHi = "बिल भरें →",
                actionTextMr = "बिल भरा →",
                iconType = "water"
            ),
            CitizenService(
                id = "lpg",
                sectionEn = "Utilities & Payments (उपयोगिता और बिल)",
                sectionHi = "उपयोगिता और बिल",
                sectionMr = "उपयुक्तता व बिले",
                titleEn = "LPG Cylinder Refill",
                titleHi = "एलपीजी गैस सिलेंडर बुकिंग",
                titleMr = "गॅस सिलिंडर बुकिंग",
                descriptionEn = "Book Indane / Bharat / HP Gas cylinder",
                descriptionHi = "इंडेन, भारत या एचपी गैस सिलेंडर बुक करें",
                descriptionMr = "इंडेन, भारत किंवा एचपी गॅस सिलिंडर बुक करा",
                actionTextEn = "Book Refill →",
                actionTextHi = "सिलेंडर बुक करें →",
                actionTextMr = "सिलिंडर बुक करा →",
                iconType = "gas"
            ),
            CitizenService(
                id = "bhulekh",
                sectionEn = "Land & Revenue Records (भूलेख व महसूल)",
                sectionHi = "भूलेख व महसूल",
                sectionMr = "जमीन महसूल नोंदी",
                titleEn = "7/12 Land Record (भूलेख)",
                titleHi = "7/12 भूलेख खसरा-खतौनी",
                titleMr = "७/१२ व ८-अ डिजिटल उतारा",
                descriptionEn = "View certified land ownership and mutation records",
                descriptionHi = "प्रमाणित भूमि स्वामित्व व नामांतरण स्थिति देखें",
                descriptionMr = "डिजिटल स्वाक्षरीत जमिनीचा ७/१२ व फेरफार पहा",
                actionTextEn = "Check Records →",
                actionTextHi = "रिकॉर्ड देखें →",
                actionTextMr = "नोंदी तपासा →",
                iconType = "land"
            ),
            CitizenService(
                id = "fasal_bima",
                sectionEn = "Land & Revenue Records (भूलेख व महसूल)",
                sectionHi = "भूलेख व महसूल",
                sectionMr = "जमीन महसूल नोंदी",
                titleEn = "PM Fasal Bima (फसल बीमा)",
                titleHi = "प्रधानमंत्री फसल बीमा योजना",
                titleMr = "पंतप्रधान पीक विमा योजना",
                descriptionEn = "Crop loss claim, policy status and compensation",
                descriptionHi = "फसल नुकसान दावा, बीमा स्थिति और मुआवजा",
                descriptionMr = "पीक नुकसान भरपाई, विमा स्थिती व क्लेम",
                actionTextEn = "Track Policy →",
                actionTextHi = "पॉलिसी ट्रैक करें →",
                actionTextMr = "विमा तपासा →",
                iconType = "crop_insurance"
            )
        )

        val defaultDigitalLessons = listOf(
            DigitalLesson(
                id = "basics",
                categoryId = "basics",
                titleEn = "Smartphone Basics",
                titleHi = "स्मार्टफोन का बुनियादी उपयोग",
                titleMr = "स्मार्टफोनचा मूलभूत वापर",
                descriptionEn = "Learn touch gestures, Wi-Fi connections, voice typing, and downloading essential apps.",
                descriptionHi = "टच जेस्चर, वाई-फाई कनेक्शन, वॉयस टाइपिंग और आवश्यक ऐप डाउनलोड करना सीखें।",
                descriptionMr = "टच हावभाव, वाय-फाय कनेक्शन, व्हॉईस टायपिंग आणि आवश्यक ॲप्स डाउनलोड करणे शिका.",
                lessonCount = 5,
                badgeEn = "5 Lessons",
                badgeHi = "5 पाठ",
                badgeMr = "५ धडे",
                contentStepsEn = listOf(
                    "Lesson 1: Using Touchscreen gestures (Tap, Double Tap, Pinch to Zoom, Long press)",
                    "Lesson 2: Making and saving voice calls and adding village contacts to favorites",
                    "Lesson 3: Connecting safely to Home/Panchayat Wi-Fi & managing Mobile Data balance",
                    "Lesson 4: Using Hindi/Marathi Voice Typing (Mic button on keyboard) to send messages",
                    "Lesson 5: Downloading verified government and farming apps safely from Google Play Store"
                ),
                contentStepsHi = listOf(
                    "पाठ 1: टचस्क्रीन जेस्चर (टैप, ज़ूम इन-आउट, लंबा प्रेस)",
                    "पाठ 2: कॉल करना, कॉन्टैक्ट सुरक्षित करना और पसंदीदा में जोड़ना",
                    "पाठ 3: वाई-फाई कनेक्ट करना और 4G मोबाइल डेटा प्रबंधित करना",
                    "पाठ 4: बोलकर टाइप करना (कीबोर्ड पर माइक का उपयोग)",
                    "पाठ 5: गूगल प्ले स्टोर से केवल आधिकारिक ऐप डाउनलोड करना"
                ),
                safetyTipsEn = listOf(
                    "Always lock your phone with a PIN or fingerprint pattern",
                    "Never install unknown .apk files sent on WhatsApp groups"
                ),
                safetyTipsHi = listOf(
                    "अपने फोन पर हमेशा पिन या फिंगरप्रिंट लॉक लगाकर रखें",
                    "व्हाट्सएप पर आई किसी भी अनजान .apk फाइल को कभी इंस्टॉल न करें"
                )
            ),
            DigitalLesson(
                id = "upi",
                categoryId = "upi",
                titleEn = "Digital Payments (UPI Simulator)",
                titleHi = "डिजिटल भुगतान (UPI सिम्युलेटर)",
                titleMr = "डिजिटल पेमेंट (UPI सिम्युलेटर)",
                descriptionEn = "Practice sending demo money safely, entering UPI PIN on secure pad, and recognizing fraud.",
                descriptionHi = "सुरक्षित तरीके से डेमो पैसे भेजना, सुरक्षित पैड पर यूपीआई पिन दर्ज करना और धोखाधड़ी पहचानना सीखें।",
                descriptionMr = "सुरक्षितपणे डेमो पैसे पाठवणे, यूपीआय पिन टाकणे व फसवणूक ओळखणे याचा सराव करा.",
                lessonCount = 4,
                badgeEn = "Interactive Practice",
                badgeHi = "प्रैक्टिस सिम्युलेटर",
                badgeMr = "इंटरॅक्टिव्ह सराव",
                contentStepsEn = listOf(
                    "Step 1: What is UPI? Unified Payments Interface connects your bank directly for instant transfers",
                    "Step 2: Scanning QR Code at village shops (Kirana, Seed store, Medical clinic)",
                    "Step 3: When is UPI PIN needed? PIN is ONLY required when SENDING money, NEVER for receiving!",
                    "Step 4: Checking transaction history and bank balance safely"
                ),
                contentStepsHi = listOf(
                    "चरण 1: यूपीआई क्या है? यह आपके बैंक खाते को तुरंत सुरक्षित भुगतान से जोड़ता है",
                    "चरण 2: गांव की दुकानों पर क्यूआर कोड स्कैन करना",
                    "चरण 3: यूपीआई पिन कब आवश्यक है? पिन केवल पैसे भेजते समय लगता है, प्राप्त करने के लिए कभी नहीं!",
                    "चरण 4: लेनदेन इतिहास और बैंक बैलेंस सुरक्षित जांचना"
                ),
                safetyTipsEn = listOf(
                    "CRITICAL RULE: You NEVER need to enter your UPI PIN to RECEIVE money. If someone asks for PIN to send you money, it is 100% A FRAUD!",
                    "Never share your UPI PIN, ATM PIN, or Bank OTP with anyone, not even bank officers or village leaders."
                ),
                safetyTipsHi = listOf(
                    "महत्वपूर्ण नियम: पैसे प्राप्त करने के लिए कभी भी यूपीआई पिन दर्ज करने की आवश्यकता नहीं होती। यदि कोई पैसे भेजने के लिए पिन मांगे, तो वह धोखा है!",
                    "अपना यूपीआई पिन, एटीएम पिन या ओटीपी किसी के साथ साझा न करें।"
                )
            ),
            DigitalLesson(
                id = "forms",
                categoryId = "forms",
                titleEn = "Online Government Forms & Portals",
                titleHi = "सरकारी ऑनलाइन फॉर्म और पोर्टल",
                titleMr = "शासकीय ऑनलाइन फॉर्म व पोर्टल्स",
                descriptionEn = "Learn how to find official portals, create login, upload certificates, and track application reference numbers.",
                descriptionHi = "आधिकारिक पोर्टल खोजना, लॉगिन बनाना, प्रमाण पत्र अपलोड करना और आवेदन की स्थिति ट्रैक करना सीखें।",
                descriptionMr = "अधिकृत वेबसाइट शोधणे, नोंदणी करणे, प्रमाणपत्रे अपलोड करणे व अर्जाची स्थिती तपासणे शिका.",
                lessonCount = 4,
                badgeEn = "Step-by-Step",
                badgeHi = "सरल चरण",
                badgeMr = "सविस्तर मार्गदर्शन",
                contentStepsEn = listOf(
                    "Lesson 1: Recognizing authentic government websites (look for .gov.in or .nic.in domain)",
                    "Lesson 2: Registering with your mobile number and Aadhaar OTP securely",
                    "Lesson 3: Taking clear photos of documents (Ration Card, Land 7/12) without blur",
                    "Lesson 4: Saving Application Acknowledgement Slip with Reference Tracking Number"
                ),
                contentStepsHi = listOf(
                    "पाठ 1: आधिकारिक सरकारी वेबसाइट की पहचान (.gov.in या .nic.in देखें)",
                    "पाठ 2: मोबाइल नंबर और आधार ओटीपी से सुरक्षित पंजीकरण",
                    "पाठ 3: दस्तावेजों (राशन कार्ड, खतौनी) की स्पष्ट फोटो लेना",
                    "पाठ 4: आवेदन पावती और रसीद नंबर सुरक्षित रखना"
                ),
                safetyTipsEn = listOf(
                    "Always verify that the website URL ends with '.gov.in' or '.nic.in'. Avoid private fake sites charging money for free schemes."
                ),
                safetyTipsHi = listOf(
                    "हमेशा जांचें कि वेबसाइट '.gov.in' पर समाप्त होती है। फर्जी निजी साइटों से बचें जो मुफ्त योजनाओं के पैसे मांगती हैं।"
                )
            ),
            DigitalLesson(
                id = "safety",
                categoryId = "safety",
                titleEn = "Internet Safety & Fraud Prevention",
                titleHi = "इंटरनेट सुरक्षा और साइबर फ्रॉड से बचाव",
                titleMr = "इंटरनेट सुरक्षा आणि सायबर फसवणुकीपासून बचाव",
                descriptionEn = "Recognize phishing, fake lottery SMS, suspicious APKs, fake bank calls, and protect your OTP.",
                descriptionHi = "फिशिंग, फर्जी लॉटरी एसएमएस, संदिग्ध एपीके, फर्जी बैंक कॉल पहचानें और अपने ओटीपी की रक्षा करें।",
                descriptionMr = "खोटे कॉल्स, बनावट लॉटरी मेसेज, संशयास्पद ॲप्स ओळखा आणि बँक खात्याची सुरक्षा राखा.",
                lessonCount = 6,
                badgeEn = "Crucial Guide",
                badgeHi = "अति आवश्यक",
                badgeMr = "महत्त्वाचे",
                contentStepsEn = listOf(
                    "Rule 1: Never open links in SMS saying 'Your electricity bill will be cut off tonight - click here'",
                    "Rule 2: Fake PM-Kisan SMS claiming '₹50,000 lottery won' are scams",
                    "Rule 3: Banks and Government NEVER call asking for OTP or ATM numbers",
                    "Rule 4: Do not download remote screen-sharing apps like AnyDesk or TeamViewer on unknown caller instructions",
                    "Rule 5: If cheated, immediately dial national cyber crime helpline 1930"
                ),
                contentStepsHi = listOf(
                    "नियम 1: 'आज रात आपकी बिजली कट जाएगी - लिंक पर क्लिक करें' जैसे एसएमएस लिंक कभी न खोलें",
                    "नियम 2: पीएम किसान या लॉटरी के फर्जी मैसेज से बचें",
                    "नियम 3: बैंक या सरकार कभी भी फोन पर ओटीपी या पिन नहीं मांगती",
                    "नियम 4: किसी अनजान व्यक्ति के कहने पर एनीडेस्क जैसी रिमोट स्क्रीन ऐप डाउनलोड न करें",
                    "नियम 5: वित्तीय धोखाधड़ी होने पर तुरंत 1930 राष्ट्रीय हेल्पलाइन पर कॉल करें"
                ),
                safetyTipsEn = listOf(
                    "Report any cyber crime immediately to 1930 within 2 hours for best chance of recovering blocked funds."
                ),
                safetyTipsHi = listOf(
                    "साइबर धोखाधड़ी होने पर 2 घंटे के भीतर तुरंत 1930 पर शिकायत दर्ज करें।"
                )
            )
        )

        val defaultQuizQuestions = listOf(
            QuizQuestion(
                id = 1,
                questionEn = "Someone calls saying they are sending you ₹2,000 under PM-Kisan, and asks you to enter your UPI PIN. What should you do?",
                questionHi = "कोई व्यक्ति फोन करके कहता है कि वह पीएम-किसान के तहत आपको ₹2,000 भेज रहा है, और आपसे यूपीआई पिन दर्ज करने को कहता है। आपको क्या करना चाहिए?",
                optionsEn = listOf(
                    "Enter the UPI PIN immediately",
                    "Refuse and disconnect the call. UPI PIN is NEVER required to receive money",
                    "Share your ATM card number instead",
                    "Ask for their Aadhaar card first"
                ),
                optionsHi = listOf(
                    "तुरंत यूपीआई पिन दर्ज करें",
                    "इनकार करें और फोन काट दें। पैसे प्राप्त करने के लिए कभी पिन नहीं लगता",
                    "इसके बदले अपने एटीएम कार्ड का नंबर दे दें",
                    "पहले उनका आधार कार्ड मांगें"
                ),
                correctIndex = 1,
                explanationEn = "Correct! You only enter UPI PIN when you want to SEND money. Receiving money never requires a PIN.",
                explanationHi = "शाबाश! सही उत्तर। यूपीआई पिन केवल पैसे भेजते समय लगता है। पैसे मिलने के लिए कभी पिन नहीं लगता।"
            ),
            QuizQuestion(
                id = 2,
                questionEn = "Which of the following is a genuine government website address?",
                questionHi = "निम्नलिखित में से कौन सी एक वास्तविक आधिकारिक सरकारी वेबसाइट है?",
                optionsEn = listOf(
                    "www.pmkisan-yojana-apply.com",
                    "www.pmkisan.gov.in",
                    "www.free-tractor-schemes.org",
                    "www.sarkar-form-help.net"
                ),
                optionsHi = listOf(
                    "www.pmkisan-yojana-apply.com",
                    "www.pmkisan.gov.in",
                    "www.free-tractor-schemes.org",
                    "www.sarkar-form-help.net"
                ),
                correctIndex = 1,
                explanationEn = "Correct! Authentic Indian government portals always end with '.gov.in' or '.nic.in'.",
                explanationHi = "बिल्कुल सही! भारत सरकार की सभी आधिकारिक वेबसाइटों के अंत में '.gov.in' या '.nic.in' होता है।"
            ),
            QuizQuestion(
                id = 3,
                questionEn = "You receive an SMS: 'Your Electricity power will be disconnected at 9:30 PM due to unpaid bill. Click link to pay.' What is the right action?",
                questionHi = "आपको एक एसएमएस मिलता है: 'बिल न भरने के कारण आज रात 9:30 बजे बिजली काट दी जाएगी। बिल भरने के लिए लिंक पर क्लिक करें।' सही कदम क्या है?",
                optionsEn = listOf(
                    "Immediately click the link and enter debit card details",
                    "Forward the message to all village WhatsApp groups",
                    "Do NOT click the link. Check official electricity DISCOM counter or portal",
                    "Call the phone number written in the SMS"
                ),
                optionsHi = listOf(
                    "तुरंत लिंक पर क्लिक करें और डेबिट कार्ड का विवरण दर्ज करें",
                    "मैसेज को गांव के सभी व्हाट्सएप ग्रुपों में भेज दें",
                    "लिंक पर क्लिक न करें। बिजली दफ्तर या आधिकारिक काउंटर पर जांचें",
                    "एसएमएस में लिखे मोबाइल नंबर पर फोन करें"
                ),
                correctIndex = 2,
                explanationEn = "Correct! This is a very common electricity scam. Always verify with your local power discom directly.",
                explanationHi = "सही! यह एक आम बिजली बिल फ्रॉड है। कभी भी अज्ञात लिंक पर क्लिक न करें।"
            ),
            QuizQuestion(
                id = 4,
                questionEn = "What is the emergency helpline number for Cyber Financial Fraud in India?",
                questionHi = "भारत में साइबर वित्तीय धोखाधड़ी की शिकायत के लिए राष्ट्रीय आपातकालीन हेल्पलाइन नंबर कौन सा है?",
                optionsEn = listOf("100", "1930", "108", "1551"),
                optionsHi = listOf("100", "1930", "108", "1551"),
                correctIndex = 1,
                explanationEn = "Correct! 1930 is the National Cyber Crime reporting helpline for financial fraud.",
                explanationHi = "सही जवाब! 1930 साइबर वित्तीय धोखाधड़ी की राष्ट्रीय हेल्पलाइन है।"
            ),
            QuizQuestion(
                id = 5,
                questionEn = "Can a bank manager or branch officer ask for your 6-digit One Time Password (OTP)?",
                questionHi = "क्या बैंक मैनेजर या शाखा अधिकारी फोन पर आपका 6 अंकों का ओटीपी (OTP) मांग सकते हैं?",
                optionsEn = listOf(
                    "Yes, if they are calling from the main branch",
                    "Yes, for annual KYC update",
                    "No! Bank staff will NEVER ask for your secret OTP",
                    "Only if they verify your account number first"
                ),
                optionsHi = listOf(
                    "हाँ, यदि वे मुख्य शाखा से बोल रहे हों",
                    "हाँ, वार्षिक केवाईसी अपडेट के लिए",
                    "नहीं! बैंक कर्मचारी कभी भी आपका गोपनीय ओटीपी नहीं मांगते",
                    "केवल तभी यदि वे खाता नंबर की पुष्टि करें"
                ),
                correctIndex = 2,
                explanationEn = "Correct! No legitimate bank employee or official will ever ask for your OTP.",
                explanationHi = "बिल्कुल सही! कोई भी असली बैंक अधिकारी कभी भी आपका ओटीपी नहीं मांगता।"
            )
        )

        val defaultCropAdvisories = listOf(
            CropAdvisory(
                id = "wheat",
                nameEn = "Wheat (गेहूं)",
                nameHi = "गेहूं",
                nameMr = "गहू",
                seasonEn = "Rabi Season (Nov - April)",
                soilRequirementsEn = "Well-drained fertile loamy to clay loamy soil with pH 6.0 to 7.5",
                waterRequirementsEn = "4-6 irrigations at Critical Root Initiation (CRI), tillering, flowering, and grain filling stages",
                basicGuidanceEn = "Sow certified disease-resistant seeds (HD-2967, DBW-187). Treat seeds with Trichoderma viride or Carboxin before sowing.",
                commonPestsEn = "Yellow rust, loose smut, aphids and armyworms",
                preventionEn = "Spray Propiconazole 25% EC @ 1ml/L at first sign of rust. Maintain proper plant spacing."
            ),
            CropAdvisory(
                id = "soybean",
                nameEn = "Soybean (सोयाबीन)",
                nameHi = "सोयाबीन",
                nameMr = "सोयाबीन",
                seasonEn = "Kharif Season (June - Oct)",
                soilRequirementsEn = "Deep, fertile, well-drained black cotton soils with good organic content",
                waterRequirementsEn = "Rainfed crop. Requires irrigation if dry spell exceeds 15 days during flowering & pod filling",
                basicGuidanceEn = "Use Rhizobium & PSB culture seed treatment. Inoculate 10kg seed with 250g bacterial culture for superior nitrogen fixation.",
                commonPestsEn = "Girdle beetle, semilooper, stem fly, and rust",
                preventionEn = "Install yellow sticky traps (10/acre). Spray Chlorantraniliprole 18.5% SC @ 0.3ml/L if caterpillar infestation exceeds threshold."
            ),
            CropAdvisory(
                id = "cotton",
                nameEn = "Cotton (कपास)",
                nameHi = "कपास",
                nameMr = "कापूस",
                seasonEn = "Kharif Season (May - Dec)",
                soilRequirementsEn = "Deep black cotton soils (regur) with moisture retention capability",
                waterRequirementsEn = "Critical stages: Square formation, flowering, and boll development (700-1200 mm water)",
                basicGuidanceEn = "Maintain 90x60 cm or 120x45 cm spacing. Monitor pheromone traps weekly for pink bollworm moths.",
                commonPestsEn = "Pink bollworm, whitefly, jassids, and thrips",
                preventionEn = "Install 4-5 pheromone traps per acre. Use Neem-based bio-pesticides (Azadirachtin 1500 ppm) in early vegetative stage."
            )
        )

        val defaultProduceBuyers = listOf(
            ProduceBuyer(
                id = "mandi_kangra",
                name = "Kangra Central APMC Mandi",
                type = BuyerType.MANDI,
                location = "Kangra Main Yard, APMC Road",
                distanceKm = 8.5,
                cropsAccepted = listOf("Wheat", "Soybean", "Gram", "Maize", "Mustard", "Tur"),
                offeredPricePerUnit = 4850.0,
                priceUnit = "₹/Qtl (Demo Price)",
                isVerified = true,
                contactPhone = "+91 98160 33445",
                rating = 4.8
            ),
            ProduceBuyer(
                id = "mandi_palampur",
                name = "Palampur Sub-Mandi Yard",
                type = BuyerType.MANDI,
                location = "Palampur Mandi Complex",
                distanceKm = 3.2,
                cropsAccepted = listOf("Vegetables", "Fruits", "Wheat", "Gram", "Onion"),
                offeredPricePerUnit = 4900.0,
                priceUnit = "₹/Qtl (Demo Price)",
                isVerified = true,
                contactPhone = "+91 98160 55667",
                rating = 4.7
            ),
            ProduceBuyer(
                id = "buyer_kisan_traders",
                name = "Kisan Agri Grain Traders & Mills",
                type = BuyerType.DIRECT_BUYER,
                location = "Kangra Industrial Area, Plot 14",
                distanceKm = 11.0,
                cropsAccepted = listOf("Soybean", "Wheat", "Mustard", "Cotton"),
                offeredPricePerUnit = 4920.0,
                priceUnit = "₹/Qtl (Demo Price)",
                isVerified = true,
                contactPhone = "+91 98765 22334",
                rating = 4.6
            ),
            ProduceBuyer(
                id = "buyer_organic_fpo",
                name = "Himachal Hill Organic Farmer Producer Co-op",
                type = BuyerType.VERIFIED_BUYER,
                location = "Palampur Organic Hub",
                distanceKm = 4.8,
                cropsAccepted = listOf("Wheat", "Gram", "Tur", "Vegetables", "Fruits"),
                offeredPricePerUnit = 5200.0,
                priceUnit = "₹/Qtl (Demo Price)",
                isVerified = true,
                contactPhone = "+91 98160 99887",
                rating = 4.9
            )
        )

        val defaultDeliveryPartners = listOf(
            DeliveryPartner(
                id = "dp_tractor_trolley",
                driverName = "Gurpreet Singh (Local Transporter)",
                vehicleType = "Tractor Trolley (5-Ton)",
                vehicleNumber = "HP-68-TR-4021",
                serviceArea = "Palampur, Kangra & Nagrota (Within 35 km)",
                estimatedFare = 650,
                availability = "Available Today",
                isVerified = true,
                phone = "+91 98765 11223",
                rating = 4.9
            ),
            DeliveryPartner(
                id = "dp_bolero_pickup",
                driverName = "Rajinder Sharma (Kisan Logistics)",
                vehicleType = "Bolero Maxi Pickup (2-Ton)",
                vehicleNumber = "HP-68-PK-9812",
                serviceArea = "All Kangra Valley & Mandis",
                estimatedFare = 850,
                availability = "Available Tomorrow Morning",
                isVerified = true,
                phone = "+91 98160 44556",
                rating = 4.8
            ),
            DeliveryPartner(
                id = "dp_tata_ace",
                driverName = "Suresh Yadav (Chhota Hathi)",
                vehicleType = "Tata Ace Mini Truck (1.5-Ton)",
                vehicleNumber = "HP-40-MT-1120",
                serviceArea = "Village Palampur & local Mandi",
                estimatedFare = 500,
                availability = "Available in 1 Hour",
                isVerified = true,
                phone = "+91 98765 77889",
                rating = 4.7
            ),
            DeliveryPartner(
                id = "dp_eicher_truck",
                driverName = "Himachal State Rural Transport Co.",
                vehicleType = "Eicher Heavy Truck (10-Ton)",
                vehicleNumber = "HP-68-HT-7744",
                serviceArea = "State-wide Inter-Mandi Transport",
                estimatedFare = 1800,
                availability = "Book 1 Day Prior",
                isVerified = true,
                phone = "+91 98160 11992",
                rating = 4.9
            )
        )

        val defaultDeliveryRequests = listOf(
            DeliveryRequest(
                id = "DEL-84920",
                produceListingId = "PROD-101",
                produceTitle = "Soybean (सोयाबीन)",
                quantity = "50.0 Quintal",
                pickupLocation = "Village Palampur, Kangra",
                destination = "Kangra Central APMC Mandi",
                pickupDate = "2026-09-30",
                pickupTime = "09:00 AM - 11:00 AM",
                vehicleType = "Tractor Trolley (5-Ton)",
                deliveryPartner = defaultDeliveryPartners[0],
                isSelfTransport = false,
                estimatedFare = 650,
                additionalInstructions = "Store in dry bags. Farm is near Panchayat Bhavan.",
                status = DeliveryStatus.ACCEPTED,
                statusUpdatedAt = "Today, 10:30 AM",
                trackingUpdates = listOf(
                    "Delivery Requested by Farmer" to "Yesterday, 04:00 PM",
                    "Accepted by Driver Gurpreet Singh (HP-68-TR-4021)" to "Today, 10:30 AM"
                )
            )
        )

        val defaultProduceListings = listOf(
            ProduceListing(
                id = "PROD-101",
                cropName = "Soybean (सोयाबीन)",
                category = "Oilseeds",
                quantity = 50.0,
                unit = "Quintal (क्विंटल)",
                expectedPricePerUnit = 4800.0,
                qualityGrade = "Grade A (Super Clean)",
                harvestDate = "2026-09-24",
                pickupLocation = "Village Palampur, Kangra",
                preferredBuyerOrMandi = "Kangra Central APMC Mandi",
                productPhoto = "🌱",
                additionalNotes = "Moisture level under 10%. Packed in standard jute bags.",
                listedDate = "25 Sep 2026",
                status = ProduceSaleStatus.DELIVERY_ACCEPTED,
                selectedBuyer = defaultProduceBuyers[0],
                deliveryRequest = defaultDeliveryRequests[0]
            ),
            ProduceListing(
                id = "PROD-102",
                cropName = "Wheat (गेहूं - HD 2967)",
                category = "Grains & Cereals",
                quantity = 35.0,
                unit = "Quintal (क्विंटल)",
                expectedPricePerUnit = 2450.0,
                qualityGrade = "Grade A (Super)",
                harvestDate = "2026-09-20",
                pickupLocation = "Village Palampur, Kangra",
                preferredBuyerOrMandi = "Palampur Sub-Mandi Yard",
                productPhoto = "🌾",
                additionalNotes = "High protein golden grain, thoroughly winnowed.",
                listedDate = "27 Sep 2026",
                status = ProduceSaleStatus.ACTIVE_LISTING
            ),
            ProduceListing(
                id = "PROD-103",
                cropName = "Gram / Chana (चना)",
                category = "Pulses",
                quantity = 15.0,
                unit = "Quintal (क्विंटल)",
                expectedPricePerUnit = 5800.0,
                qualityGrade = "Grade B (Standard)",
                harvestDate = "2026-09-15",
                pickupLocation = "Village Palampur, Kangra",
                preferredBuyerOrMandi = "Himachal Hill Organic Farmer Producer Co-op",
                productPhoto = "🫘",
                additionalNotes = "Desi bold chana, pesticide-free.",
                listedDate = "18 Sep 2026",
                status = ProduceSaleStatus.COMPLETED,
                selectedBuyer = defaultProduceBuyers[3]
            )
        )

        val defaultFarmEquipmentList = listOf(
            FarmEquipment(
                id = "eq_mahindra_575",
                nameEn = "Mahindra 575 DI Tractor (45 HP)",
                nameHi = "महिंद्रा 575 डीआई ट्रैक्टर (45 एचपी)",
                nameMr = "महिंद्रा 575 डीआय ट्रॅक्टर (45 HP)",
                category = EquipmentCategory.TRACTORS,
                providerName = "Kangra Custom Hiring Center (CHC)",
                location = "Palampur Rural Hub, 4.2 km",
                distanceKm = 4.2,
                priceAmount = 750,
                pricingUnit = "hour",
                isDemoPrice = true,
                availabilityStatus = "Available Today",
                isVerifiedProvider = true,
                descriptionEn = "Heavy duty 45 HP 4-cylinder tractor suitable for ploughing, rotavator, trolley haulage, and sowing operations.",
                descriptionHi = "जुताई, रोटावेटर, ट्रॉली परिवहन और बुवाई कार्यों के लिए उपयुक्त 45 एचपी 4-सिलेंडर शक्तिशाली ट्रैक्टर।",
                specifications = listOf(
                    "Engine Power" to "45 HP @ 1900 RPM",
                    "Hydraulic Lift" to "1600 kg capacity",
                    "Operator" to "Experienced Driver Included",
                    "Fuel" to "Diesel included in fare"
                ),
                termsEn = "Minimum booking 2 hours. Driver provided. Overtime charged per 30 minutes.",
                rating = 4.9,
                iconEmoji = "🚜"
            ),
            FarmEquipment(
                id = "eq_kubota_harvester",
                nameEn = "Kubota Multi-Crop Combine Harvester",
                nameHi = "कुबोटा कंबाइन हार्वेस्टर (धान व गेहूं)",
                nameMr = "कुबोटा कंबाईन हार्वेस्टर",
                category = EquipmentCategory.HARVESTERS,
                providerName = "Himachal Agri Services Co-op",
                location = "Nagrota Yard, 7.5 km",
                distanceKm = 7.5,
                priceAmount = 1800,
                pricingUnit = "acre",
                isDemoPrice = true,
                availabilityStatus = "Available from Tomorrow",
                isVerifiedProvider = true,
                descriptionEn = "Rubber track combine harvester for efficient cutting, threshing, and cleaning of wheat and paddy crops with minimal grain loss.",
                descriptionHi = "गेहूं और धान की तेजी से कटाई और मड़ाई के लिए रबर ट्रैक कंबाइन हार्वेस्टर। न्यूनतम दाना बर्बादी।",
                specifications = listOf(
                    "Cutter Bar Width" to "2.0 meters",
                    "Crops Handled" to "Wheat, Paddy, Soybean, Mustard",
                    "Speed" to "1.5 to 2.0 acres/hour",
                    "Operator" to "2 Trained Technicians Included"
                ),
                termsEn = "Charges calculated per acre measured on field. Dry field condition required.",
                rating = 4.8,
                iconEmoji = "🌾"
            ),
            FarmEquipment(
                id = "eq_shaktiman_rotavator",
                nameEn = "Shaktiman Heavy Rotavator (7 Feet)",
                nameHi = "शक्तिमान रोटावेटर (7 फीट - 48 ब्लेड)",
                nameMr = "शक्तिमान रोटाव्हेटर (7 फूट)",
                category = EquipmentCategory.ROTAVATORS,
                providerName = "Palampur Kisan Krishi Sewa",
                location = "Kangra Road, 3.0 km",
                distanceKm = 3.0,
                priceAmount = 600,
                pricingUnit = "hour",
                isDemoPrice = true,
                availabilityStatus = "Available Today",
                isVerifiedProvider = true,
                descriptionEn = "48 L-type blade heavy duty rotary tiller for fine seedbed preparation in single pass.",
                descriptionHi = "एक ही फेरे में उत्तम मिट्टी भुरभुरी बनाने के लिए 48 एल-टाइप ब्लेड वाला रोटावेटर।",
                specifications = listOf(
                    "Tilling Width" to "7 Feet (210 cm)",
                    "Blades" to "48 Boron Steel L-type",
                    "Required Tractor" to "45 HP and above"
                ),
                termsEn = "Attaches to standard 3-point hitch. Can be rented with or without tractor.",
                rating = 4.7,
                iconEmoji = "⚙️"
            ),
            FarmEquipment(
                id = "eq_seed_drill",
                nameEn = "Automatic Multi-Crop Seed-Cum-Fertilizer Drill",
                nameHi = "स्वचालित बीज एवं खाद बुवाई यंत्र (सीड ड्रिल)",
                nameMr = "स्वयंचलित बियाणे व खत पेरणी यंत्र",
                category = EquipmentCategory.SEEDERS,
                providerName = "Rana Agro Services",
                location = "Bhawarna Hub, 5.8 km",
                distanceKm = 5.8,
                priceAmount = 550,
                pricingUnit = "acre",
                isDemoPrice = true,
                availabilityStatus = "Available Today",
                isVerifiedProvider = true,
                descriptionEn = "9-row precision seed and fertilizer metering unit ensuring uniform depth and seed spacing for wheat, gram, and soybean.",
                descriptionHi = "गेहूं, चना और सोयाबीन की समान गहराई और दूरी पर बुवाई के लिए 9-पंक्ति का परिशुद्ध सीड ड्रिल।",
                specifications = listOf(
                    "Rows" to "9 Tynes with Fluted Roller metering",
                    "Hopper Capacity" to "Seed 50kg + Fertilizer 50kg",
                    "Row Spacing" to "Adjustable 18-22 cm"
                ),
                termsEn = "Calibrated on farm before sowing. Fertilizer must be dry and granulated.",
                rating = 4.8,
                iconEmoji = "🌱"
            ),
            FarmEquipment(
                id = "eq_multi_thresher",
                nameEn = "Multi-Crop High Output Thresher",
                nameHi = "मल्टी-क्रॉप थ्रेशर मशीन (गेहूं, चना, सरसों)",
                nameMr = "मल्टी-क्रॉप मळणी यंत्र",
                category = EquipmentCategory.THRESHERS,
                providerName = "Kangra CHC Machinery",
                location = "Dharamshala Link Road, 9.0 km",
                distanceKm = 9.0,
                priceAmount = 700,
                pricingUnit = "hour",
                isDemoPrice = true,
                availabilityStatus = "Available on Booking",
                isVerifiedProvider = true,
                descriptionEn = "Dual-fan high capacity thresher with elevator and bagging attachment for clean grain separation.",
                descriptionHi = "गेहूं, चना और मक्का की त्वरित मड़ाई और सफाई के लिए डबल फैन थ्रेशर।",
                specifications = listOf(
                    "Capacity" to "12-15 Quintals grain/hour",
                    "Power Source" to "Tractor PTO driven (35+ HP)",
                    "Fans" to "2 Blower fans for zero dust"
                ),
                termsEn = "Fuel charged as per tractor hours. Safety guard mandatory during operation.",
                rating = 4.7,
                iconEmoji = "🌾"
            ),
            FarmEquipment(
                id = "eq_boom_sprayer",
                nameEn = "Tractor Mounted Boom Power Sprayer (500L)",
                nameHi = "ट्रैक्टर माउंटेड बूम स्प्रेयर (500 लीटर टैंक)",
                nameMr = "ट्रॅक्टर बूम फवारणी यंत्र (500L)",
                category = EquipmentCategory.SPRAYERS,
                providerName = "Kisan Vikas Agro Center",
                location = "Palampur Central, 2.5 km",
                distanceKm = 2.5,
                priceAmount = 450,
                pricingUnit = "acre",
                isDemoPrice = true,
                availabilityStatus = "Available Today",
                isVerifiedProvider = true,
                descriptionEn = "12-meter wide hydraulic boom sprayer with anti-drip ceramic nozzles for uniform pesticide and liquid fertilizer application.",
                descriptionHi = "12 मीटर चौड़ा बूम स्प्रेयर जो कीटनाशक और नैनो यूरिया का समान छिड़काव करता है।",
                specifications = listOf(
                    "Tank Capacity" to "500 Liters UV stabilized",
                    "Boom Width" to "12 meters (covering 24 rows)",
                    "Pump" to "Triplex piston pump @ 40 bar"
                ),
                termsEn = "Water must be arranged at farm. Protective safety gear provided to operator.",
                rating = 4.9,
                iconEmoji = "💧"
            )
        )

        val defaultEquipmentBookings = listOf(
            EquipmentBooking(
                id = "BKG-73912",
                equipmentId = "eq_mahindra_575",
                equipmentName = "Mahindra 575 DI Tractor (45 HP)",
                providerName = "Kangra Custom Hiring Center (CHC)",
                bookingDate = "2026-09-30",
                timeSlot = "08:00 AM - 12:00 PM (Morning)",
                durationOrArea = "4 Hours",
                farmLocation = "Village Palampur, Field #12",
                instructions = "Field deep ploughing for Rabi wheat preparation.",
                estimatedCost = 3000,
                status = EquipmentBookingStatus.SCHEDULED,
                statusDate = "Scheduled for Tomorrow",
                providerPhone = "",
                iconEmoji = "🚜"
            ),
            EquipmentBooking(
                id = "BKG-61044",
                equipmentId = "eq_shaktiman_rotavator",
                equipmentName = "Shaktiman Heavy Rotavator (7 Feet)",
                providerName = "Palampur Kisan Krishi Sewa",
                bookingDate = "2026-09-22",
                timeSlot = "02:00 PM - 05:00 PM",
                durationOrArea = "3 Hours",
                farmLocation = "Village Palampur, North Plot",
                instructions = "Soil pulverization completed successfully.",
                estimatedCost = 1800,
                status = EquipmentBookingStatus.COMPLETED,
                statusDate = "22 Sep 2026",
                providerPhone = "+91 98160 55667",
                iconEmoji = "⚙️"
            )
        )
    }
}

