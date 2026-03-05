package com.example.myapplication.network

import com.example.myapplication.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // AUTH
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Map<String, String>>

    @GET("api/auth/whoami")
    suspend fun whoami(): Response<Map<String, UserInfo>>

    // FESTIVALS
    @GET("api/festivals")
    suspend fun getFestivals(): Response<List<Festival>>

    @GET("api/festivals/{id}")
    suspend fun getFestival(@Path("id") id: Int): Response<Festival>

    @GET("api/festivals/{id}/games")
    suspend fun getFestivalGames(@Path("id") id: Int): Response<List<Jeu>>

    // RÉSERVATIONS
    @GET("api/reservations/festival/{festivalId}")
    suspend fun getReservationsByFestival(@Path("festivalId") festivalId: Int): Response<List<Reservation>>

    @GET("api/reservations/{id}")
    suspend fun getReservation(@Path("id") id: Int): Response<Reservation>

    // ÉDITEURS
    @GET("api/editeurs")
    suspend fun getEditeurs(): Response<List<Editeur>>

    // JEUX
    @GET("api/jeux")
    suspend fun getJeux(): Response<List<Jeu>>

    // ZONES TARIFAIRES
    @GET("api/zones-tarifaires")
    suspend fun getZonesTarifaires(): Response<List<TariffZone>>
}
