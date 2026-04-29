package com.example.anytws

import android.app.Application
import android.content.Intent
import android.util.Log

class PersistentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) Log.d(TAG, "onCreate")

        HeadsetManager.getInstance(this)

        startService(Intent(this, AnyService::class.java))
    }

    companion object {
        private val TAG = PersistentApplication::class.java.simpleName
        private const val DEBUG = true
    }
}
