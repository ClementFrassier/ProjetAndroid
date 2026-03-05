package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AuthManager
import com.example.myapplication.data.AuthRepository
import com.example.myapplication.data.FestivalRepository
import com.example.myapplication.data.ReservationRepository
import com.example.myapplication.network.ApiClient

class AwiApplication : Application() {

    lateinit var authManager: AuthManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var festivalRepository: FestivalRepository
        private set

    lateinit var reservationRepository: ReservationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        authManager = AuthManager(this)
        val apiService = ApiClient.create(authManager)
        authRepository = AuthRepository(apiService, authManager)
        festivalRepository = FestivalRepository(apiService)
        reservationRepository = ReservationRepository(apiService)
    }
}
