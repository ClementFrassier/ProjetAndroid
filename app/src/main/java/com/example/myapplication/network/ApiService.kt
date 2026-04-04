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
    suspend fun whoami(): Response<AuthResponse>

    // FESTIVALS
    @GET("api/festivals")
    suspend fun getFestivals(): Response<List<Festival>>

    @GET("api/festivals/{id}")
    suspend fun getFestival(@Path("id") id: Int): Response<Festival>

    @POST("api/festivals")
    suspend fun createFestival(@Body request: CreateFestivalRequest): Response<FestivalResponse>

    @GET("api/festivals/{id}/games")
    suspend fun getFestivalGames(@Path("id") id: Int): Response<List<Jeu>>

    // RÉSERVATIONS
    @GET("api/reservations/festival/{festivalId}")
    suspend fun getReservationsByFestival(@Path("festivalId") festivalId: Int): Response<List<Reservation>>

    @GET("api/reservations/{id}")
    suspend fun getReservation(@Path("id") id: Int): Response<Reservation>

    @POST("api/reservations")
    suspend fun createReservation(@Body reservation: ReservationCreateInput): Response<ReservationResponse>

    @PUT("api/reservations/{id}")
    suspend fun updateReservation(@Path("id") id: Int, @Body reservation: ReservationUpdateInput): Response<ReservationResponse>

    @DELETE("api/reservations/{id}")
    suspend fun deleteReservation(@Path("id") id: Int): Response<Unit>

    // EDITORS
    @GET("api/editeurs")
    suspend fun getEditors(): Response<List<Editor>>

    @GET("api/editeurs/{id}")
    suspend fun getEditorById(@Path("id") id: Int): Response<EditorDetail>

    @GET("api/editeurs/{id}/jeux")
    suspend fun getEditorGames(@Path("id") id: Int): Response<List<Game>>

    @POST("api/editeurs")
    suspend fun createEditor(@Body editor: EditorInput): Response<EditorResponse>

    @PUT("api/editeurs/{id}")
    suspend fun updateEditor(@Path("id") id: Int, @Body editor: EditorInput): Response<EditorResponse>

    // GAMES
    @GET("api/jeux")
    suspend fun getGames(@Query("editeur_id") editorId: Int? = null): Response<List<GameWithEditor>>

    @GET("api/jeux/{id}")
    suspend fun getGameById(@Path("id") id: Int): Response<GameWithEditor>

    @POST("api/jeux")
    suspend fun createGame(@Body game: GameCreateInput): Response<GameResponse>

    @PUT("api/jeux/{id}")
    suspend fun updateGame(@Path("id") id: Int, @Body game: GameInput): Response<GameResponse>

    @DELETE("api/jeux/{id}")
    suspend fun deleteGame(@Path("id") id: Int): Response<Unit>

    // INVOICES
    @GET("api/factures/reservation/{reservationId}")
    suspend fun getInvoiceByReservation(@Path("reservationId") reservationId: Int): Response<Invoice>

    @POST("api/reservations/{reservationId}/factures")
    suspend fun createInvoiceForReservation(@Path("reservationId") reservationId: Int): Response<InvoiceResponse>

    @PUT("api/factures/{id}/payee")
    suspend fun markInvoiceAsPaid(@Path("id") id: Int): Response<InvoiceResponse>

    // ZONES TARIFAIRES
    @GET("api/zones-tarifaires")
    suspend fun getZonesTarifaires(): Response<List<TariffZone>>

    @GET("api/zone-plans")
    suspend fun getZonePlans(@Query("festival_id") festivalId: Int): Response<List<ZonePlan>>

    @POST("api/zone-plans")
    suspend fun createZonePlan(@Body input: ZonePlanInput): Response<ZonePlanResponse>

    @PATCH("api/zone-plans/{id}")
    suspend fun updateZonePlan(@Path("id") id: Int, @Body input: ZonePlanUpdateInput): Response<ZonePlanResponse>

    @DELETE("api/zone-plans/{id}")
    suspend fun deleteZonePlan(@Path("id") id: Int): Response<Map<String, String>>

    @GET("api/jeu_festival")
    suspend fun getReservationGamePlacements(@Query("reservation_id") reservationId: Int): Response<List<ReservationGamePlacement>>

    @POST("api/jeu_festival")
    suspend fun createReservationGamePlacement(@Body input: ReservationGamePlacementCreateInput): Response<ReservationGamePlacementResponse>

    @PUT("api/jeu_festival/{id}")
    suspend fun updateReservationGamePlacement(@Path("id") id: Int, @Body input: ReservationGamePlacementUpdateInput): Response<ReservationGamePlacementResponse>

    @DELETE("api/jeu_festival/{id}")
    suspend fun deleteReservationGamePlacement(@Path("id") id: Int): Response<Map<String, String>>

    // USERS
    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>

    @POST("api/users")
    suspend fun createUser(@Body user: CreateUserInput): Response<User>

    @PATCH("api/users/{id}/role")
    suspend fun updateUserRole(@Path("id") id: Int, @Body payload: UpdateUserRoleInput): Response<User>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>
}
