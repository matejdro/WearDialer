plugins {
   id("com.android.application")
   id("dagger.hilt.android.plugin")
   kotlin("android")
   kotlin("kapt")
}

android {
   compileSdk = 32

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
      sourceCompatibility(JavaVersion.VERSION_1_8)
      targetCompatibility(JavaVersion.VERSION_1_8)
   }

   composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.androidx.compose.asProvider().get()
   }

   kotlinOptions {
      jvmTarget = "1.8"
   }
}


dependencies {
   implementation(project(":common"))

   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.compose.compiler)
   implementation(libs.androidx.compose.wear.foundation)
   implementation(libs.androidx.compose.wear.material)
   implementation(libs.androidx.compose.wear.navigation)
   implementation(libs.androidx.wear)
   implementation(libs.dagger.hilt.runtime)
   implementation(libs.kotlin.coroutines.playServices)
   implementation(libs.logcat)
   implementation(libs.playServices.wearable)

   kapt(libs.dagger.hilt.compiler)
}
