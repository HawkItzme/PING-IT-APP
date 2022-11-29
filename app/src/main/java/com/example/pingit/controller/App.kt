package com.example.pingit.controller

import android.app.Application
import android.content.SharedPreferences
import com.example.pingit.Utilities.SharedPrefs

class App: Application() {

    companion object{
        lateinit var prefs: SharedPrefs
    }
    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}