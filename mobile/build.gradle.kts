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

   compileOptions {
      sourceCompatibility(JavaVersion.VERSION_17)
      targetCompatibility(JavaVersion.VERSION_17)

      isCoreLibraryDesugaringEnabled = true
   }

   buildFeatures {
      compose = true
   }

   composeOptions {
      kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
   }
}

dependencies {
   implementation(project(":common"))
   implementation(project(":wearutils"))

   implementation(libs.androidx.activity)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.lifecycle.viewmodel)
   implementation(libs.androidx.compose.foundation)
   implementation(libs.androidx.compose.material)
   implementation(libs.androidx.compose.ui)
   implementation(libs.androidx.room.runtime)
   implementation(libs.androidx.room.ktx)
   implementation(libs.dagger.hilt.runtime)
   implementation(libs.kotlin.coroutines.playServices)
   implementation(libs.logcat)
   implementation(libs.playServices.wearable)
   coreLibraryDesugaring(libs.androidx.desugarJdkLibs)

   kapt(libs.androidx.room.compiler)
   kapt(libs.dagger.hilt.compiler)
}
