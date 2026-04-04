package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AuthManager
import com.example.myapplication.data.AuthRepository
import com.example.myapplication.data.EditorRepository
import com.example.myapplication.data.FestivalRepository
import com.example.myapplication.data.GameRepository
import com.example.myapplication.data.InvoiceRepository
import com.example.myapplication.data.ReservationPlacementRepository
import com.example.myapplication.data.ReservationRepository
import com.example.myapplication.data.ZonePlanRepository
import com.example.myapplication.network.ApiClient

class AwiApplication : Application() {

    lateinit var authManager: AuthManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var festivalRepository: FestivalRepository
        private set

    lateinit var editorRepository: EditorRepository
        private set

    lateinit var gameRepository: GameRepository
        private set

    lateinit var reservationRepository: ReservationRepository
        private set

    lateinit var invoiceRepository: InvoiceRepository
        private set

    lateinit var zonePlanRepository: ZonePlanRepository
        private set

    lateinit var reservationPlacementRepository: ReservationPlacementRepository
        private set

    override fun onCreate() {
        super.onCreate()
        authManager = AuthManager(this)
        val apiService = ApiClient.create(authManager)
        authRepository = AuthRepository(apiService, authManager)
        festivalRepository = FestivalRepository(apiService)
        editorRepository = EditorRepository(apiService)
        gameRepository = GameRepository(apiService)
        reservationRepository = ReservationRepository(apiService)
        invoiceRepository = InvoiceRepository(apiService)
        zonePlanRepository = ZonePlanRepository(apiService)
        reservationPlacementRepository = ReservationPlacementRepository(apiService)
    }
}
