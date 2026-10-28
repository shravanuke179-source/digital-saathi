package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    MARATHI("mr", "Marathi", "मराठी")
}

enum class NavDestination(val labelEn: String, val labelHi: String, val labelMr: String) {
    HOME("Home", "मुख्य पृष्ठ", "मुख्यपृष्ठ"),
    SERVICES("Services", "सेवाएं", "सेवा"),
    SCHEMES("Schemes", "योजनाएं", "योजना"),
    KHETI("Kheti Hub", "खेती हब", "शेती हब"),
    SHIKSHA("Digital Shiksha", "डिजिटल शिक्षा", "डिजिटल शिक्षण"),
    VILLAGE_SOS("Village SOS", "ग्राम SOS", "गाव SOS"),
    PROFILE("Profile", "प्रोफ़ाइल", "प्रोफाइल"),
    SELL_PRODUCE("Sell Farm Produce", "फसल बेचें", "शेती माल विका"),
    FARM_EQUIPMENT("Farm Equipment", "कृषि उपकरण", "शेती अवजारे"),
    MY_FARM_SALES("My Farm Sales", "मेरी फसल बिक्री", "माझी शेतमाल विक्री"),
    DELIVERY_TRACKING("Delivery Tracking", "डिलीवरी ट्रैकिंग", "डिलिव्हरी ट्रॅकिंग"),
    EQUIPMENT_BOOKINGS("Equipment Bookings", "उपकरण बुकिंग", "अवजारे बुकिंग")
}

enum class SchemeCategory(val labelEn: String, val labelHi: String, val labelMr: String) {
    ALL("All Schemes", "सभी योजनाएं", "सर्व योजना"),
    FARMERS("Farmers (किसान)", "किसान योजनाएं", "शेतकरी योजना"),
    WOMEN("Women (महिला)", "महिला कल्याण", "महिला कल्याण"),
    STUDENTS("Students (विद्यार्थी)", "विद्यार्थी छात्रवृत्ति", "विद्यार्थी शिष्यवृत्ती"),
    WORKERS("Workers (श्रमिक)", "श्रमिक कल्याण", "कामगार कल्याण"),
    SENIOR_CITIZENS("Senior Citizens (वरिष्ठ)", "वरिष्ठ नागरिक", "ज्येष्ठ नागरिक")
}

data class Scheme(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val titleMr: String,
    val category: SchemeCategory,
    val descriptionEn: String,
    val descriptionHi: String,
    val descriptionMr: String,
    val benefitEn: String,
    val benefitHi: String,
    val benefitMr: String,
    val eligibilityEn: List<String>,
    val eligibilityHi: List<String>,
    val eligibilityMr: List<String>,
    val documentsEn: List<String>,
    val documentsHi: List<String>,
    val documentsMr: List<String>,
    val applicationProcessEn: String,
    val applicationProcessHi: String,
    val officialLink: String,
    val iconType: String = "tractor", // tractor, water, seed, farmer, woman, student, worker, senior
    val isActive: Boolean = true,
    var isBookmarked: Boolean = false
)

data class MandiPrice(
    val id: String,
    val commodityEn: String,
    val commodityHi: String,
    val commodityMr: String,
    val mandiEn: String,
    val mandiHi: String,
    val mandiMr: String,
    val modalPrice: Int,
    val unit: String = "Qtl",
    val minPrice: Int,
    val maxPrice: Int,
    val dailyTrend: Int, // e.g. +40, -30
    val dateUpdated: String = "Today",
    val arrivalQuantity: String = "450 Qtl",
    val stateEn: String = "Maharashtra",
    val isLive: Boolean = true
)

data class WeatherData(
    val locationName: String = "Palampur, Himachal Pradesh",
    val temperature: Double = 28.0,
    val feelsLike: Double = 29.5,
    val humidity: Int = 65,
    val windSpeed: Double = 12.0,
    val weatherCode: Int = 1,
    val conditionEn: String = "Partly Cloudy",
    val conditionHi: String = "आंशिक रूप से बादल",
    val conditionMr: String = "अंशतः ढगाळ",
    val iconEmoji: String = "🌤️",
    val maxTemp: Double = 32.0,
    val minTemp: Double = 22.0,
    val rainProbability: Int = 15,
    val farmingAdviceEn: String = "Ideal conditions for irrigation and crop spraying.",
    val farmingAdviceHi: String = "सिंचाई और कीटनाशक छिड़काव के लिए अनुकूल मौसम।",
    val farmingAdviceMr: String = "सिंचन आणि कीटकनाशक फवारणीसाठी योग्य हवामान.",
    val lastUpdatedTime: String = "Live • Just now",
    val isLiveApi: Boolean = true
)

data class CropAdvisory(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val nameMr: String,
    val seasonEn: String,
    val soilRequirementsEn: String,
    val waterRequirementsEn: String,
    val basicGuidanceEn: String,
    val commonPestsEn: String,
    val preventionEn: String
)

data class CitizenService(
    val id: String,
    val sectionEn: String,
    val sectionHi: String,
    val sectionMr: String,
    val titleEn: String,
    val titleHi: String,
    val titleMr: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val descriptionMr: String,
    val actionTextEn: String,
    val actionTextHi: String,
    val actionTextMr: String,
    val iconType: String // aadhaar, pan, ration, electric, water, gas, land, crop_insurance
)

data class DigitalLesson(
    val id: String,
    val categoryId: String, // basics, upi, forms, safety
    val titleEn: String,
    val titleHi: String,
    val titleMr: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val descriptionMr: String,
    val lessonCount: Int,
    val badgeEn: String,
    val badgeHi: String,
    val badgeMr: String,
    val contentStepsEn: List<String>,
    val contentStepsHi: List<String>,
    val safetyTipsEn: List<String>,
    val safetyTipsHi: List<String>
)

data class VillageNotice(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val titleMr: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val descriptionMr: String,
    val authorityEn: String,
    val authorityHi: String,
    val authorityMr: String,
    val timestampEn: String,
    val timestampHi: String,
    val timestampMr: String,
    val iconType: String // water, health, meeting, alert, agri
)

data class UserProfile(
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

data class SupabaseSyncStatus(
    val isConnected: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncMessage: String = "Cloud Database Connected • Synced with Supabase",
    val tableName: String = "public.user_profiles",
    val recordsCount: Int = 1
)

data class SubmittedApplication(
    val id: String,
    val schemeTitle: String,
    val schemeTitleHi: String,
    val appId: String,
    val appliedDate: String,
    val status: String = "In Review",
    val progressPercent: Float = 0f,
    val applicantName: String = "",
    val phone: String = "",
    val aadhaarLastFour: String = ""
)

data class EmergencyContact(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val nameMr: String,
    val number: String,
    val iconType: String, // police, ambulance, women, kisan, disaster
    val colorHex: Long
)

data class PanchayatGrievance(
    val id: String,
    val category: String,
    val subject: String,
    val description: String,
    val applicantName: String,
    val phone: String,
    val date: String,
    val status: String = "Saved — pending official submission"
)

data class QuizQuestion(
    val id: Int,
    val questionEn: String,
    val questionHi: String,
    val optionsEn: List<String>,
    val optionsHi: List<String>,
    val correctIndex: Int,
    val explanationEn: String,
    val explanationHi: String
)

// ==========================================
// FARMER SERVICES: SELL PRODUCE & EQUIPMENT
// ==========================================

enum class ProduceSaleStatus(val labelEn: String, val labelHi: String, val labelMr: String) {
    ACTIVE_LISTING("Active Listing", "सक्रिय सूची", "सक्रिय यादी"),
    BUYER_SELECTED("Buyer Selected", "क्रेता चयनित", "खरेदीदार निवडला"),
    DELIVERY_REQUESTED("Delivery Requested", "परिवहन अनुरोध भेजा", "डिलिव्हरी विनंती पाठवली"),
    DELIVERY_ACCEPTED("Delivery Accepted", "परिवहन स्वीकृत", "डिलिव्हरी स्वीकारली"),
    PICKUP_SCHEDULED("Pickup Scheduled", "पिकअप निर्धारित", "पिकअप नियोजित"),
    PICKED_UP("Picked Up", "माल उठाया गया", "माल उचलला"),
    IN_TRANSIT("In Transit", "मार्ग में है", "वाटेत आहे"),
    DELIVERED("Delivered", "पहुंचा दिया गया", "पोहोचवले"),
    COMPLETED("Sale Completed", "बिक्री पूर्ण", "विक्री पूर्ण")
}

enum class BuyerType(val labelEn: String, val labelHi: String, val labelMr: String) {
    MANDI("Nearby APMC Mandi", "निकटतम APMC मंडी", "जवळची कृषी उत्पन्न बाजार समिती"),
    DIRECT_BUYER("Direct Local Buyer / Mill", "सीधा स्थानीय खरीदार / मिल", "थेट स्थानिक खरेदीदार / मिल"),
    VERIFIED_BUYER("Verified Institutional Buyer", "सत्यापित संस्थागत खरीदार", "प्रमाणित खरेदीदार")
}

data class ProduceBuyer(
    val id: String,
    val name: String,
    val type: BuyerType,
    val location: String,
    val distanceKm: Double,
    val cropsAccepted: List<String>,
    val offeredPricePerUnit: Double? = null,
    val priceUnit: String = "₹/Qtl (Demo Price)",
    val isVerified: Boolean = true,
    val contactPhone: String = "+91 98160 12345",
    val rating: Double = 4.8
)

enum class DeliveryStatus(val labelEn: String, val labelHi: String, val labelMr: String, val stepIndex: Int) {
    REQUESTED("Requested", "अनुरोध भेजा गया", "विनंती पाठवली", 0),
    ACCEPTED("Accepted", "स्वीकार किया गया", "स्वीकारली", 1),
    PICKUP_SCHEDULED("Pickup Scheduled", "पिकअप निर्धारित", "पिकअप नियोजित", 2),
    PICKED_UP("Picked Up", "माल उठाया गया", "माल उचलला", 3),
    IN_TRANSIT("In Transit", "मार्ग में है", "वाटेत आहे", 4),
    DELIVERED("Delivered", "पहुंचा दिया गया", "पोहोचवले", 5)
}

data class DeliveryPartner(
    val id: String,
    val driverName: String,
    val vehicleType: String, // Tractor Trolley, Pickup, Mini Truck, Heavy Truck
    val vehicleNumber: String,
    val serviceArea: String,
    val estimatedFare: Int,
    val availability: String,
    val isVerified: Boolean = true,
    val phone: String = "+91 98765 11223",
    val rating: Double = 4.9
)

data class DeliveryRequest(
    val id: String,
    val produceListingId: String,
    val produceTitle: String,
    val quantity: String,
    val pickupLocation: String,
    val destination: String,
    val pickupDate: String,
    val pickupTime: String,
    val vehicleType: String,
    val deliveryPartner: DeliveryPartner? = null,
    val isSelfTransport: Boolean = false,
    val estimatedFare: Int = 850,
    val additionalInstructions: String = "",
    val status: DeliveryStatus = DeliveryStatus.REQUESTED,
    val statusUpdatedAt: String = "Just now",
    val trackingUpdates: List<Pair<String, String>> = emptyList() // Status to Timestamp
)

data class ProduceListing(
    val id: String,
    val cropName: String,
    val category: String, // Grains & Cereals, Oilseeds, Pulses, Commercial, Vegetables & Fruits
    val quantity: Double,
    val unit: String = "Quintal (क्विंटल)",
    val expectedPricePerUnit: Double,
    val qualityGrade: String = "Grade A (Super)",
    val harvestDate: String,
    val pickupLocation: String,
    val preferredBuyerOrMandi: String,
    val productPhoto: String = "🌾",
    val additionalNotes: String = "",
    val listedDate: String,
    val status: ProduceSaleStatus = ProduceSaleStatus.ACTIVE_LISTING,
    val selectedBuyer: ProduceBuyer? = null,
    val deliveryRequest: DeliveryRequest? = null
)

enum class EquipmentCategory(val labelEn: String, val labelHi: String, val labelMr: String) {
    ALL("All Machinery", "सभी मशीनें", "सर्व यंत्रे"),
    TRACTORS("Tractors", "ट्रैक्टर", "ट्रॅक्टर"),
    HARVESTERS("Harvesters", "हार्वेस्टर", "हार्वेस्टर"),
    ROTAVATORS("Rotavators", "रोटावेटर", "रोटाव्हेटर"),
    CULTIVATORS("Cultivators", "कल्टीवेटर व हल", "नांगर व कल्टीव्हेटर"),
    SEEDERS("Seed Drills", "बीज बुवाई यंत्र", "पेरणी यंत्र"),
    THRESHERS("Threshers", "थ्रेशर मशीन", "मळणी यंत्र"),
    TROLLEYS("Tractor Trolley", "ट्रैक्टर ट्रॉली", "ट्रॅक्टर ट्रॉली"),
    SPRAYERS("Sprayers", "छिड़काव मशीन", "फवारणी यंत्र")
}

data class FarmEquipment(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val nameMr: String,
    val category: EquipmentCategory,
    val providerName: String,
    val location: String,
    val distanceKm: Double,
    val priceAmount: Int,
    val pricingUnit: String = "hour", // hour, acre, day
    val isDemoPrice: Boolean = true,
    val availabilityStatus: String = "Available Today",
    val isVerifiedProvider: Boolean = true,
    val descriptionEn: String,
    val descriptionHi: String,
    val specifications: List<Pair<String, String>>,
    val termsEn: String,
    val termsHi: String = "ईंधन और ऑपरेटर शुल्क शामिल हैं। न्यूनतम बुकिंग 2 घंटे।",
    val rating: Double = 4.8,
    val iconEmoji: String = "🚜"
)

enum class EquipmentBookingStatus(val labelEn: String, val labelHi: String, val labelMr: String) {
    REQUESTED("Requested", "अनुरोध भेजा", "विनंती पाठवली"),
    ACCEPTED("Accepted", "स्वीकृत", "स्वीकारली"),
    SCHEDULED("Scheduled", "निर्धारित", "नियोजित"),
    COMPLETED("Completed", "पूर्ण", "पूर्ण"),
    CANCELLED("Cancelled", "रद्द किया", "रद्द केले")
}

data class EquipmentBooking(
    val id: String,
    val equipmentId: String,
    val equipmentName: String,
    val providerName: String,
    val bookingDate: String,
    val timeSlot: String,
    val durationOrArea: String,
    val farmLocation: String,
    val instructions: String = "",
    val estimatedCost: Int,
    val status: EquipmentBookingStatus = EquipmentBookingStatus.REQUESTED,
    val statusDate: String = "Today",
    val providerPhone: String = "",
    val iconEmoji: String = "🚜"
)

