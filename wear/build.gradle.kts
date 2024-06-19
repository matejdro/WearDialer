plugins {
   id("com.android.application")
   id("dagger.hilt.android.plugin")
   kotlin("android")
   kotlin("kapt")
   alias(libs.plugins.composeCompiler)
}

android {
   compileSdk = 34
   namespace = "com.matejdro.weardialer"

   defaultConfig {
      applicationId = "com.matejdro.weardialer"
      minSdk = 26
      targetSdk = 32

      versionCode = 1
      versionName = "1.0"
   }

   buildFeatures {
      compose = true
   }

   compileOptions {
      sourceCompatibility(JavaVersion.VERSION_17)
      targetCompatibility(JavaVersion.VERSION_17)
   }

   buildTypes {
      debug {
         // Deploy optimized version to speed it up
         isDebuggable = false
         isMinifyEnabled = true
         proguardFiles.add(getDefaultProguardFile("proguard-android-optimize.txt"))
         proguardFiles.add(file("proguard-rules.pro"))
      }
   }
}

kotlin {
   jvmToolchain(17)
}

dependencies {
   implementation(project(":common"))
   implementation(project(":wearutils"))

   implementation(libs.androidDateTimeFormatters)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.compose.compiler)
   implementation(libs.androidx.compose.wear.foundation)
   implementation(libs.androidx.compose.wear.material)
   implementation(libs.androidx.compose.wear.navigation)
   debugImplementation(libs.androidx.compose.ui.tooling)
   implementation(libs.androidx.wear)
   implementation(libs.dagger.hilt.runtime)
   implementation(libs.kotlin.coroutines.playServices)
   implementation(libs.logcat)
   implementation(libs.playServices.wearable)

   kapt(libs.dagger.hilt.compiler)
}
