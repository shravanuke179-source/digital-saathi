from pathlib import Path
base=Path('app/src/main/java/com/example')
p=base/'data/model/Models.kt';s=p.read_text(encoding='utf-8')
s=s.replace('val progressPercent: Float = 0.45f','val progressPercent: Float = 0f,\n    val applicantName: String = "",\n    val phone: String = "",\n    val aadhaarLastFour: String = ""')
s=s.replace('val status: String = "Submitted to Gram Panchayat"','val status: String = "Saved — pending official submission"')
s=s.replace('val providerPhone: String = "+91 98765 44332"','val providerPhone: String = ""')
p.write_text(s,encoding='utf-8')
p=base/'data/repository/DigitalSaathiRepository.kt';s=p.read_text(encoding='utf-8')
s=s.replace('            progressPercent = 0f\n','            progressPercent = 0f,\n            applicantName = applicantName.trim(),\n            phone = phone.trim(),\n            aadhaarLastFour = aadhaar.filter { it.isDigit() }.takeLast(4)\n')
s=s.replace('            providerPhone = "+91 98160 88990",','            providerPhone = "",')
s=s.replace('            deliveryPartner = deliveryPartner,','            deliveryPartner = if (isSelfTransport) null else deliveryPartner,')
s=s.replace('''    fun loginAsDemo() {
        _authError.value = "Sign in or create a free account to access private data. Practice tools remain available after sign-in."
    }''','''    fun loginAsDemo() = authenticate({}, {}) { it.signInAnonymously().awaitResult() }''')
s=s.replace('''try { if (auth?.currentUser?.uid == uid) saveProfile(initial) }''','''try {
                            if (auth?.currentUser?.uid == uid) {
                                val ref = db.document("users/$uid")
                                db.runTransaction { transaction ->
                                    if (!transaction.get(ref).exists()) transaction.set(ref, mapOf(
                                        "name" to initial.name, "village" to initial.village, "district" to initial.district,
                                        "phone" to initial.phone, "state" to initial.state))
                                }.awaitResult()
                            }
                        }''')
s=s.replace('        _selectedLanguage.value = language\n', '        _selectedLanguage.value = language\n        savePreferences()\n')
s=s.replace('        _selectedLocation.value = location\n', '        _selectedLocation.value = location\n        savePreferences()\n')
s=s.replace('        _isGpsActive.value = isLiveGps\n', '        _isGpsActive.value = isLiveGps\n        savePreferences()\n')
s=s.replace('        _currentDestination.value = destination\n', '        if (_isAuthenticated.value) _currentDestination.value = destination\n')
s=s.replace('    private fun requireUid():', '''    private fun savePreferences() {
        if (auth?.currentUser == null) return
        val uid = requireUid()
        val settings = mapOf("language" to _selectedLanguage.value.name, "location" to _selectedLocation.value,
            "latitude" to _userCoordinates.value.first, "longitude" to _userCoordinates.value.second, "state" to _detectedState.value)
        runMutation { requireNotNull(database).document("users/$uid/preferences/settings").set(settings).awaitResult() }
    }

    private fun requireUid():''')
s=s.replace('        fun <T : Any> watch(path:', '''        listeners += db.document("users/$uid/preferences/settings").addSnapshotListener { snapshot, error ->
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
        fun <T : Any> watch(path:''')
s=s.replace('        _isGpsActive.value = false\n', '        _isGpsActive.value = false\n        _detectedState.value = "Himachal Pradesh"\n        _selectedLanguage.value = AppLanguage.ENGLISH\n')
# Only display actual server measurements as live.
s=s.replace('val current = response.current','val current = requireNotNull(response.current) { "Weather readings unavailable" }')
s=s.replace('val temp = current?.temperature ?: 28.0','val temp = requireNotNull(current.temperature) { "Temperature unavailable" }')
p.write_text(s,encoding='utf-8')
changes={
'ui/screens/AuthScreen.kt':{'⚡ Quick Citizen Demo Access (त्वरित नागरिक प्रवेश)':'⚡ Continue as Guest (अतिथि प्रवेश)'},
'ui/screens/ProfileScreen.kt':{
'User data is connected and synced with Supabase PostgreSQL cloud table':'User data is stored in Firebase Cloud Firestore at',
'Text("Linked & Verified (UIDAI)",':'Text(if (userProfile.isVerified) "Verified" else "Not verified",'},
'ui/components/SchemeDetailDialog.kt':{
'Application Submitted!':'Application Saved!',
'Verified Village:':'Village:',
'Aadhaar Number':'Aadhaar last 4 digits (optional)',
},
'ui/components/GrievanceDialog.kt':{
'Grievance Registered!':'Grievance Saved!',
'शिकायत ग्राम पंचायत में दर्ज कर ली गई है':'शिकायत सहेजी गई है। आधिकारिक पोर्टल पर जमा करें।',
'Status: Forwarded to Sarpanch & Gram Sevak':'Status: Saved; official submission still required',
'Submit a community issue directly to the Gram Panchayat Sarpanch and Gram Sevak.':'Save a community issue in your account. Submit it through your official Panchayat channel.',
},
'ui/screens/FarmEquipmentScreen.kt':{
'🚜 Booking requested successfully! Provider will confirm shortly.':'Booking request saved. Provider confirmation is pending.',
},
'ui/screens/SellProduceScreen.kt':{
'🚚 Delivery request sent successfully!':'Delivery request saved. Transport confirmation is pending.',
},
}
for file,replacements in changes.items():
 p=base/file;s=p.read_text(encoding='utf-8')
 for old,new in replacements.items():s=s.replace(old,new)
 p.write_text(s,encoding='utf-8')
p=base/'MainActivity.kt';s=p.read_text(encoding='utf-8')
s=s.replace('    DigitalSaathiTheme(darkTheme = isDarkMode) {','    LaunchedEffect(isAuthenticated) {\n        if (!isAuthenticated) { isSearchOpen = false; selectedSchemeForDetail = null }\n    }\n\n    DigitalSaathiTheme(darkTheme = isDarkMode) {')
p.write_text(s,encoding='utf-8')
