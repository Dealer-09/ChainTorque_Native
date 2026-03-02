package com.example.chaintorquenative

import android.app.Application

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChainTorqueApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}
