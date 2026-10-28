package com.example.data.network

import com.example.data.model.UserProfile
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseUserProfileDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "village") val village: String? = null,
    @Json(name = "district") val district: String? = null,
    @Json(name = "state") val state: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "ration_card_no") val rationCardNo: String? = null,
    @Json(name = "bank_linked") val bankLinked: String? = null,
    @Json(name = "auth_provider") val authProvider: String? = null,
    @Json(name = "is_verified") val isVerified: Boolean? = true,
    @Json(name = "updated_at") val updatedAt: String? = null
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            name = name ?: "Ramesh Kumar",
            email = email ?: "ramesh.kumar@gmail.com",
            village = village ?: "Village Palampur",
            district = district ?: "District Kangra",
            state = state ?: "Himachal Pradesh",
            phone = phone ?: "+91 98765 43210",
            firebaseUid = userId ?: "cit-ramesh-kumar-8392",
            rationCardNo = rationCardNo ?: "RC-9876-HP",
            bankLinked = bankLinked ?: "State Bank of India (A/C ...4829)",
            isVerified = isVerified ?: true,
            authProvider = authProvider ?: "Google Account"
        )
    }

    companion object {
        fun fromDomain(userProfile: UserProfile): SupabaseUserProfileDto {
            return SupabaseUserProfileDto(
                userId = userProfile.firebaseUid,
                name = userProfile.name,
                email = userProfile.email,
                village = userProfile.village,
                district = userProfile.district,
                state = userProfile.state,
                phone = userProfile.phone,
                rationCardNo = userProfile.rationCardNo,
                bankLinked = userProfile.bankLinked,
                authProvider = userProfile.authProvider,
                isVerified = userProfile.isVerified
            )
        }
    }
}
