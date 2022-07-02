package com.matejdro.weardialer.wear

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import logcat.AndroidLogcatLogger

@HiltAndroidApp
class WearApplication : Application() {
   override fun onCreate() {
      super.onCreate()

      AndroidLogcatLogger.installOnDebuggableApp(this)
   }
}
