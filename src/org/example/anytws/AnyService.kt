package com.example.anytws

import android.app.Service
import android.content.Intent
import android.util.Log

class AnyService : Service() {

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (DEBUG) Log.d(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent) = null

    companion object {
        private val TAG = AnyService::class.java.simpleName
        private const val DEBUG = true
    }
}
