package com.example.data.network

import com.example.data.model.MandiPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class KhetiMarketApiService {

    private val baseCommodities = listOf(
        BaseCommodity(
            id = "mandi_wheat_nagpur",
            commodityEn = "Wheat (Lokwan / शरबती)",
            commodityHi = "गेहूं (लोकवान / शरबती)",
            commodityMr = "गहू (लोकवान / शरबती)",
            mandiEn = "Nagpur APMC, Maharashtra",
            mandiHi = "नागपुर एपीएमसी, महाराष्ट्र",
            mandiMr = "नागपूर एपीएमसी, महाराष्ट्र",
            baseModal = 2580,
            baseMin = 2420,
            baseMax = 2740,
            arrivalBase = 840,
            stateEn = "Maharashtra"
        ),
        BaseCommodity(
            id = "mandi_soybean_nagpur",
            commodityEn = "Soybean (Yellow / पीला)",
            commodityHi = "सोयाबीन (पीला सोना)",
            commodityMr = "सोयाबीन (पिवळे सोने)",
            mandiEn = "Nagpur & Hingna APMC",
            mandiHi = "नागपुर एवं हिंगणा मंडी",
            mandiMr = "नागपूर व हिंगणा मंडी",
            baseModal = 4650,
            baseMin = 4380,
            baseMax = 4820,
            arrivalBase = 1420,
            stateEn = "Maharashtra"
        ),
        BaseCommodity(
            id = "mandi_cotton_amravati",
            commodityEn = "Cotton (Medium Staple / कपास)",
            commodityHi = "कपास (मध्यम रेशा)",
            commodityMr = "कापूस (मध्यम धागा)",
            mandiEn = "Amravati & Wardha APMC",
            mandiHi = "अमरावती व वर्धा मंडी",
            mandiMr = "अमरावती व वर्धा मंडी",
            baseModal = 7420,
            baseMin = 7100,
            baseMax = 7850,
            arrivalBase = 620,
            stateEn = "Maharashtra"
        ),
        BaseCommodity(
            id = "mandi_chana_indore",
            commodityEn = "Gram / Chana (Desi / चना)",
            commodityHi = "चना (देशी काबुली)",
            commodityMr = "हरभरा / चणा (देशी)",
            mandiEn = "Indore Mandi, MP",
            mandiHi = "इंदौर मंडी, मध्य प्रदेश",
            mandiMr = "इंदूर मंडी, मध्य प्रदेश",
            baseModal = 6150,
            baseMin = 5800,
            baseMax = 6380,
            arrivalBase = 950,
            stateEn = "Madhya Pradesh"
        ),
        BaseCommodity(
            id = "mandi_onion_lasalgaon",
            commodityEn = "Onion (Nashik Red / प्याज)",
            commodityHi = "प्याज (नासिक लाल)",
            commodityMr = "कांदा (नाशिक लाल)",
            mandiEn = "Lasalgaon APMC, Nashik",
            mandiHi = "लासलगांव एपीएमसी, नासिक",
            mandiMr = "लासलगाव एपीएमसी, नाशिक",
            baseModal = 1850,
            baseMin = 1400,
            baseMax = 2200,
            arrivalBase = 3200,
            stateEn = "Maharashtra"
        ),
        BaseCommodity(
            id = "mandi_tomato_pune",
            commodityEn = "Tomato (Hybrid / टमाटर)",
            commodityHi = "टमाटर (हाइब्रिड)",
            commodityMr = "टोमॅटो (हायब्रिड)",
            mandiEn = "Pune & Narayangaon APMC",
            mandiHi = "पुणे एवं नारायणगांव मंडी",
            mandiMr = "पुणे व नारायणगाव मंडी",
            baseModal = 1450,
            baseMin = 1100,
            baseMax = 1750,
            arrivalBase = 1800,
            stateEn = "Maharashtra"
        ),
        BaseCommodity(
            id = "mandi_mustard_jaipur",
            commodityEn = "Mustard (Sarson / सरसों)",
            commodityHi = "सरसों (रायड़ा / पीली)",
            commodityMr = "मोहरी (पिवळी / काळी)",
            mandiEn = "Jaipur & Alwar Mandi, RJ",
            mandiHi = "जयपुर व अलवर मंडी, राजस्थान",
            mandiMr = "जयपूर व अलवार मंडी, राजस्थान",
            baseModal = 5620,
            baseMin = 5350,
            baseMax = 5890,
            arrivalBase = 780,
            stateEn = "Rajasthan"
        ),
        BaseCommodity(
            id = "mandi_paddy_khanna",
            commodityEn = "Paddy / Rice (Basmati / धान)",
            commodityHi = "धान (बासमती पूसा)",
            commodityMr = "भात (बासमती पूसा)",
            mandiEn = "Khanna Grain Market, Punjab",
            mandiHi = "खन्ना अनाज मंडी, पंजाब",
            mandiMr = "खन्ना धान्य बाजार, पंजाब",
            baseModal = 3850,
            baseMin = 3600,
            baseMax = 4100,
            arrivalBase = 2100,
            stateEn = "Punjab"
        ),
        BaseCommodity(
            id = "mandi_maize_palampur",
            commodityEn = "Maize (Makka / मक्का)",
            commodityHi = "मक्का (पीला दाना)",
            commodityMr = "मका (पिवळा दाणा)",
            mandiEn = "Palampur & Kangra Market",
            mandiHi = "पालमपुर व कांगड़ा मंडी",
            mandiMr = "पालमपूर व कांगडा मंडी",
            baseModal = 2240,
            baseMin = 2050,
            baseMax = 2390,
            arrivalBase = 410,
            stateEn = "Himachal Pradesh"
        ),
        BaseCommodity(
            id = "mandi_groundnut_rajkot",
            commodityEn = "Groundnut (Moongfali / मूंगफली)",
            commodityHi = "मूंगफली (बोल्ड दाना)",
            commodityMr = "शेंगदाणा (बोल्ड दाणा)",
            mandiEn = "Rajkot APMC, Gujarat",
            mandiHi = "राजकोट एपीएमसी, गुजरात",
            mandiMr = "राजकोट एपीएमसी, गुजरात",
            baseModal = 6850,
            baseMin = 6400,
            baseMax = 7200,
            arrivalBase = 1100,
            stateEn = "Gujarat"
        )
    )

    suspend fun fetchLiveMandiPrices(): List<MandiPrice> = withContext(Dispatchers.IO) {
        // Network latency simulation
        delay(400)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTimeStr = "Live • ${timeFormat.format(Date())}"

        baseCommodities.map { item ->
            // Add live fluctuation (+/- 2% random shift)
            val trendDelta = Random.nextInt(-45, 65)
            val modal = (item.baseModal + trendDelta).coerceAtLeast(100)
            val min = (modal - Random.nextInt(100, 250)).coerceAtLeast(50)
            val max = (modal + Random.nextInt(120, 300))
            val arrivals = item.arrivalBase + Random.nextInt(-80, 150)

            MandiPrice(
                id = item.id,
                commodityEn = item.commodityEn,
                commodityHi = item.commodityHi,
                commodityMr = item.commodityMr,
                mandiEn = item.mandiEn,
                mandiHi = item.mandiHi,
                mandiMr = item.mandiMr,
                modalPrice = modal,
                unit = "Qtl",
                minPrice = min,
                maxPrice = max,
                dailyTrend = trendDelta,
                dateUpdated = currentTimeStr,
                arrivalQuantity = "$arrivals Qtl",
                stateEn = item.stateEn,
                isLive = true
            )
        }
    }

    private data class BaseCommodity(
        val id: String,
        val commodityEn: String,
        val commodityHi: String,
        val commodityMr: String,
        val mandiEn: String,
        val mandiHi: String,
        val mandiMr: String,
        val baseModal: Int,
        val baseMin: Int,
        val baseMax: Int,
        val arrivalBase: Int,
        val stateEn: String
    )
}
