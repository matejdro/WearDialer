package com.matejdro.weardialer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import logcat.AndroidLogcatLogger

@HiltAndroidApp
class MobileApplication : Application() {
   override fun onCreate() {
      super.onCreate()

      AndroidLogcatLogger.installOnDebuggableApp(this)
   }
}
