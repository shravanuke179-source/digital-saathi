from pathlib import Path
import re

root = Path(__file__).parent
base = root / 'app/src/main/java/com/example'
def edit(path, fn):
    p = root / path
    p.write_text(fn(p.read_text(encoding='utf-8')), encoding='utf-8')

edit('app/build.gradle.kts', lambda s: s.replace('// implementation(libs.firebase.firestore)', 'implementation(libs.firebase.firestore)').replace('// implementation(libs.firebase.auth)', 'implementation(libs.firebase.auth)').replace('// implementation(libs.androidx.credentials)', 'implementation(libs.androidx.credentials)').replace('// implementation(libs.androidx.credentials.play.services)', 'implementation(libs.androidx.credentials.play.services)').replace('// implementation(libs.googleid)', 'implementation(libs.googleid)').replace('debug { signingConfig = signingConfigs.getByName("debugConfig") }', 'debug { /* Use the standard local Android debug keystore. */ }').replace('  alias(libs.plugins.secrets)\n', ''))
edit('app/build.gradle.kts', lambda s: re.sub(r'// Configure the Secrets Gradle Plugin.*?\n}\n', '// Private service credentials must never be generated into BuildConfig.\n', s, flags=re.S))

p = base / 'data/model/Models.kt'
s = p.read_text(encoding='utf-8')
start, end = s.index('data class UserProfile('), s.index('data class SupabaseSyncStatus(')
s = s[:start] + '''data class UserProfile(
    val name: String = "",
    val email: String = "",
    val village: String = "",
    val district: String = "",
    val state: String = "",
    val phone: String = "",
    val firebaseUid: String = "",
    val rationCardNo: String = "",
    val bankLinked: String = "",
    val isVerified: Boolean = false,
    val authProvider: String = "",
    val lastSyncedWithSupabase: String = "Not synced",
    val isSupabaseSynced: Boolean = false
)

''' + s[end:]
p.write_text(s, encoding='utf-8')

p = base / 'data/repository/DigitalSaathiRepository.kt'
s = p.read_text(encoding='utf-8')
s = s.replace('import java.util.Locale', '''import java.util.Locale
import java.util.UUID
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock''')
s = s.replace('CoroutineScope(Dispatchers.IO + SupervisorJob())', 'CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())')
for flow, typ in [('notices','VillageNotice'), ('submittedApplications','SubmittedApplication'), ('produceListings','ProduceListing'), ('deliveryRequests','DeliveryRequest'), ('equipmentBookings','EquipmentBooking'), ('produceBuyers','ProduceBuyer'), ('deliveryPartners','DeliveryPartner'), ('farmEquipmentList','FarmEquipment'), ('mandiPrices','MandiPrice')]:
    s = re.sub(r'MutableStateFlow\(default' + flow[0].upper()+flow[1:] + r'\)', f'MutableStateFlow<List<{typ}>>(emptyList())', s)
s = s.replace('lastUpdatedTime = "Live • Just now",\n            isLiveApi = true', 'lastUpdatedTime = "Sample — waiting for weather",\n            isLiveApi = false', 1)
s = s.replace('// Fallback to updated simulated live reading on error', '// Keep the previous reading but explicitly mark it stale.')
s = s.replace('lastUpdatedTime = timeStr,\n                    isLiveApi = true\n                )\n            } finally', 'lastUpdatedTime = "Weather unavailable — previous/sample reading",\n                    isLiveApi = false\n                )\n            } finally')
start, end = s.index('    fun refreshMandiPrices('), s.index('    private fun getInterpretation')
s = s[:start] + '''    fun refreshMandiPrices(preferredState: String = _detectedState.value) {
        // Published market quotes stream from Firestore; never randomize production prices.
        _mandiPrices.value = _mandiPrices.value.sortedByDescending { it.stateEn == preferredState }
    }

''' + s[end:]
s = s.replace('isConnected = true,\n            isSyncing = false,\n            lastSyncMessage = "Cloud Database Connected • Table: public.user_profiles",\n            tableName = "public.user_profiles",\n            recordsCount = 1', 'isConnected = false,\n            isSyncing = false,\n            lastSyncMessage = "Firebase not connected",\n            tableName = "users/{uid}",\n            recordsCount = 0')
start, end = s.index('    fun syncWithSupabase('), s.index('    // ==========================================\n    // FARM PRODUCE')
s = s[:start] + '''    // Retained names preserve the existing profile component API.
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

    fun loginAsDemo() {
        _authError.value = "Sign in or create a free account to access private data. Practice tools remain available after sign-in."
    }

    fun logout() {
        auth?.signOut()
        clearSession()
    }

''' + s[end:]
# real IDs, owner-scoped persisted entities
s = s.replace('val randomNum = (100000..999999).random()', 'val randomNum = UUID.randomUUID().toString()')
s = s.replace('"GRV-${(1000..9999).random()}"', '"GRV-${UUID.randomUUID()}"')
s = re.sub(r'\$\{System.currentTimeMillis\(\)(?: % 100000)?\}', '${UUID.randomUUID()}', s)
for name in ['submitSchemeApplication','addVillageNotice','submitGrievance','addProduceListing','requestDelivery','bookEquipment']:
    s = s.replace('    fun '+name+'(', '    suspend fun '+name+'(')
s = s.replace('        _submittedApplications.value = listOf(newApp)', '        require(applicantName.isNotBlank() && phone.length in 7..30) { "Enter your name and phone number." }\n        saveRecord("applications", newApp.id, newApp, SubmittedApplication::class.java)\n        _submittedApplications.value = listOf(newApp)')
s = s.replace('status = "In Review",\n            progressPercent = 0.35f', 'status = "Saved — submit through official portal",\n            progressPercent = 0f')
s = s.replace('        _notices.value = listOf(newNotice)', '        require(title.isNotBlank() && description.isNotBlank()) { "Enter a title and notice details." }\n        requireNotNull(store) { "Firebase not configured" }.save("notices/${newNotice.id}", newNotice, VillageNotice::class.java)\n        _notices.value = listOf(newNotice)')
s = s.replace('        _grievances.value = listOf(grievance)', '        require(subject.isNotBlank() && description.isNotBlank()) { "Enter a subject and description." }\n        saveRecord("grievances", grievance.id, grievance, PanchayatGrievance::class.java)\n        _grievances.value = listOf(grievance)')
s = s.replace('        _produceListings.value = listOf(newListing)', '        require(cropName.isNotBlank() && quantity.isFinite() && quantity > 0 && expectedPricePerUnit.isFinite() && expectedPricePerUnit > 0) { "Enter a crop, positive quantity and price." }\n        saveRecord("produce", newListing.id, newListing, ProduceListing::class.java)\n        _produceListings.value = listOf(newListing)')
s = s.replace('        _deliveryRequests.value = listOf(newRequest)', '''        require(targetListing != null && pickupDate.isNotBlank() && destination.isNotBlank()) { "Select a listing, date and destination." }
        val uid = requireUid()
        val cloud = requireNotNull(store)
        val updatedListing = targetListing.copy(deliveryRequest = newRequest, status = ProduceSaleStatus.DELIVERY_REQUESTED)
        val db = requireNotNull(database)
        db.runBatch { batch ->
            batch.set(db.document("users/$uid/deliveries/${newRequest.id}"), cloud.encode(newRequest, DeliveryRequest::class.java))
            batch.set(db.document("users/$uid/produce/$produceId"), cloud.encode(updatedListing, ProduceListing::class.java))
        }.awaitResult()
        check(auth?.currentUser?.uid == uid) { "Session changed." }
        _deliveryRequests.value = listOf(newRequest)''')
s = s.replace('        _equipmentBookings.value = listOf(newBooking)', '        require(equipment != null && bookingDate.isNotBlank() && farmLocation.isNotBlank() && cost >= 0) { "Choose equipment, a date and farm location." }\n        saveRecord("bookings", newBooking.id, newBooking, EquipmentBooking::class.java)\n        _equipmentBookings.value = listOf(newBooking)')
# Replace locally fabricated provider transitions with reading authorized backend updates.
start,end=s.index('    fun advanceDeliveryStatus('),s.index('    fun deleteProduceListing(')
s=s[:start]+'''    fun advanceDeliveryStatus(requestId: String) {
        _cloudMessage.value = "Tracking updates are published by the authorized delivery provider."
    }

'''+s[end:]
start,end=s.index('    fun deleteProduceListing('),s.index('    // ==========================================\n    // FARM EQUIPMENT')
s=s[:start]+'''    fun deleteProduceListing(produceId: String) = runMutation {
        val uid = requireUid()
        requireNotNull(store).delete("users/$uid/produce/$produceId")
        if (auth?.currentUser?.uid == uid) _produceListings.value = _produceListings.value.filterNot { it.id == produceId }
    }

'''+s[end:]
start,end=s.index('    fun selectBuyerForProduce('),s.index('    suspend fun requestDelivery(')
s=s[:start]+'''    fun selectBuyerForProduce(produceId: String, buyer: ProduceBuyer) = runMutation {
        val listing = _produceListings.value.first { it.id == produceId }
        val updated = listing.copy(selectedBuyer = buyer, status = ProduceSaleStatus.BUYER_SELECTED)
        saveRecord("produce", produceId, updated, ProduceListing::class.java)
        _produceListings.value = _produceListings.value.map { if (it.id == produceId) updated else it }
    }

'''+s[end:]
start,end=s.index('    fun cancelEquipmentBooking('),s.index('    companion object {')
s=s[:start]+'''    fun cancelEquipmentBooking(bookingId: String) = runMutation {
        val updated = _equipmentBookings.value.first { it.id == bookingId }.copy(status = EquipmentBookingStatus.CANCELLED)
        saveRecord("bookings", bookingId, updated, EquipmentBooking::class.java)
        _equipmentBookings.value = _equipmentBookings.value.map { if (it.id == bookingId) updated else it }
    }

'''+s[end:]
start,end=s.index('    fun updateProfile('),s.index('    // Retained names')
s=s[:start]+'''    fun updateProfile(name: String, village: String, district: String, phone: String) = runMutation {
        require(name.trim().length in 1..100 && village.length <= 120 && district.length <= 120 && phone.length <= 30) { "Check your profile details." }
        saveProfile(_userProfile.value.copy(name = name.trim(), village = village.trim(), district = district.trim(), phone = phone.trim()))
    }

'''+s[end:]
start,end=s.index('    fun toggleSchemeBookmark('),s.index('    suspend fun submitSchemeApplication(')
s=s[:start]+'''    fun toggleSchemeBookmark(schemeId: String) = runMutation {
        val uid = requireUid()
        val selected = _schemes.value.first { it.id == schemeId }
        val ref = requireNotNull(database).document("users/$uid/bookmarks/$schemeId")
        if (selected.isBookmarked) ref.delete().awaitResult()
        else ref.set(mapOf("schemeId" to schemeId)).awaitResult()
    }

'''+s[end:]
# Session declarations must initialize BEFORE init {}.
index=s.index('    init {')
session=(root/'firebase_session_fragment.txt').read_text(encoding='utf-8')
s=s[:index]+session+'\n'+s[index:]
s=s.replace('    init {\n        refreshAllRealTimeData()', '    init {\n        auth?.addAuthStateListener(authListener)\n        if (auth == null) _authError.value = "Firebase setup required. Follow FIREBASE_SETUP.md."\n        refreshAllRealTimeData()')
p.write_text(s,encoding='utf-8')

# Real Google account picker; retain the original button and form styling.
p=base/'ui/screens/AuthScreen.kt'
s=p.read_text(encoding='utf-8')
s=s.replace('    var showGoogleAccountPicker by remember { mutableStateOf(false) }', '    val context = androidx.compose.ui.platform.LocalContext.current')
s=s.replace('.clickable { showGoogleAccountPicker = true }', '.clickable(enabled = !isLoading) { repository.loginWithGoogle(context) }')
start,end=s.index('    // Google Account Selector Dialog'),s.index('    // Language Picker Dialog')
s=s[:start]+s[end:]
s=s.replace('Sign In with Username or Password','Sign In with Email and Password').replace('Username or Gmail Address','Email Address').replace('Protected by Supabase Cloud Database & Government 256-Bit Security','Protected by Firebase Authentication')
s=s.replace('                    // "OR" Divider', '''                    TextButton(onClick = { repository.resetPassword(loginUsernameOrEmail) }, enabled = !isLoading) {
                        Text("Forgot password?")
                    }
                    // "OR" Divider''')
p.write_text(s,encoding='utf-8')

# Lifecycle disposal, and global cloud feedback in the existing scaffold.
p=base/'MainActivity.kt'
s=p.read_text(encoding='utf-8').replace('    val currentDestination by', '    DisposableEffect(repository) { onDispose { repository.close() } }\n    val cloudMessage by repository.cloudMessage.collectAsState()\n    val cloudBusy by repository.cloudBusy.collectAsState()\n    val snackbar = remember { SnackbarHostState() }\n    LaunchedEffect(cloudMessage) { cloudMessage?.let { snackbar.showSnackbar(it); repository.clearCloudMessage() } }\n\n    val currentDestination by',1)
s=s.replace('            Scaffold(\n', '            Scaffold(\n                snackbarHost = { SnackbarHost(snackbar) },\n',1)
s=s.replace('                when (currentDestination) {','                if (cloudBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())\n                when (currentDestination) {',1)
p.write_text(s,encoding='utf-8')
