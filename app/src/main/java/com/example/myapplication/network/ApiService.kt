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
    @GET("api/reservations")
    suspend fun getReservationsByFestival(@Query("festivalId") festivalId: Int): Response<List<Reservation>>

    @GET("api/reservations/{id}")
    suspend fun getReservation(@Path("id") id: Int): Response<Reservation>

    @POST("api/reservations")
    suspend fun createReservation(@Body reservation: ReservationCreateInput): Response<Reservation>

    @PUT("api/reservations/{id}")
    suspend fun updateReservation(@Path("id") id: Int, @Body reservation: ReservationUpdateInput): Response<Reservation>

    @DELETE("api/reservations/{id}")
    suspend fun deleteReservation(@Path("id") id: Int): Response<Unit>

    // EDITORS
    @GET("api/editors")
    suspend fun getEditors(): Response<List<Editor>>

    @GET("api/editors/{id}")
    suspend fun getEditorById(@Path("id") id: Int): Response<EditorDetail>

    @POST("api/editors")
    suspend fun createEditor(@Body editor: EditorInput): Response<EditorDetail>

    @PUT("api/editors/{id}")
    suspend fun updateEditor(@Path("id") id: Int, @Body editor: EditorInput): Response<EditorDetail>

    @DELETE("api/editors/{id}")
    suspend fun deleteEditor(@Path("id") id: Int): Response<Unit>

    // GAMES
    @GET("api/games")
    suspend fun getGames(@Query("editorId") editorId: Int? = null): Response<List<GameWithEditor>>

    @GET("api/games/{id}")
    suspend fun getGameById(@Path("id") id: Int): Response<GameWithEditor>

    @POST("api/games")
    suspend fun createGame(@Body game: GameCreateInput): Response<GameWithEditor>

    @PUT("api/games/{id}")
    suspend fun updateGame(@Path("id") id: Int, @Body game: GameInput): Response<GameWithEditor>

    @DELETE("api/games/{id}")
    suspend fun deleteGame(@Path("id") id: Int): Response<Unit>

    // INVOICES
    @GET("api/invoices")
    suspend fun getInvoices(
        @Query("festivalId") festivalId: Int? = null,
        @Query("editorId") editorId: Int? = null,
        @Query("isPaid") isPaid: Boolean? = null
    ): Response<List<Invoice>>

    @GET("api/invoices/{id}")
    suspend fun getInvoiceById(@Path("id") id: Int): Response<Invoice>

    @POST("api/invoices")
    suspend fun createInvoice(@Body invoice: InvoiceCreateInput): Response<Invoice>

    @PATCH("api/invoices/{id}/pay")
    suspend fun markInvoiceAsPaid(@Path("id") id: Int): Response<Invoice>

    @DELETE("api/invoices/{id}")
    suspend fun deleteInvoice(@Path("id") id: Int): Response<Unit>

    // ZONES TARIFAIRES
    @GET("api/zones-tarifaires")
    suspend fun getZonesTarifaires(): Response<List<TariffZone>>

    // USERS
    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>

    @POST("api/users")
    suspend fun createUser(@Body user: CreateUserInput): Response<User>

    @PATCH("api/users/{id}/role")
    suspend fun updateUserRole(@Path("id") id: Int, @Body payload: UpdateUserRoleInput): Response<User>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    // RESERVANTS
    @GET("api/reservants")
    suspend fun getReservants(
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("festivalId") festivalId: Int? = null
    ): Response<List<Reservant>>

    @GET("api/reservants/{id}")
    suspend fun getReservantById(@Path("id") id: Int): Response<Reservant>

    @POST("api/reservants")
    suspend fun createReservant(@Body reservant: CreateReservantInput): Response<Reservant>

    @PUT("api/reservants/{id}")
    suspend fun updateReservant(@Path("id") id: Int, @Body reservant: UpdateReservantInput): Response<Reservant>

    @DELETE("api/reservants/{id}")
    suspend fun deleteReservant(@Path("id") id: Int): Response<Unit>

    @PUT("api/reservants/{id}/festivals/{festivalId}/status")
    suspend fun updateReservantStatus(
        @Path("id") id: Int,
        @Path("festivalId") festivalId: Int,
        @Body payload: UpdateReservantStatusInput
    ): Response<Unit>

    @POST("api/reservants/{id}/festivals/{festivalId}/contacts")
    suspend fun addReservantContact(
        @Path("id") id: Int,
        @Path("festivalId") festivalId: Int,
        @Body payload: AddReservantContactInput
    ): Response<Unit>
}
