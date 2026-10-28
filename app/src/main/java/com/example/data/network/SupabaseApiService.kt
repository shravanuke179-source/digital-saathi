package com.example.data.network

import retrofit2.http.*

interface SupabaseApiService {

    @GET("rest/v1/user_profiles")
    suspend fun getUserProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authBearer: String,
        @Query("user_id") userIdQuery: String = "eq.cit-ramesh-kumar-8392",
        @Query("select") select: String = "*"
    ): List<SupabaseUserProfileDto>

    @POST("rest/v1/user_profiles")
    suspend fun upsertUserProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authBearer: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body profile: SupabaseUserProfileDto
    ): List<SupabaseUserProfileDto>
}
