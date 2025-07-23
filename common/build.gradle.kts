plugins {
   id("com.android.library")
   id("kotlin-android")
   id("com.squareup.wire")
}

android {
   compileSdk = 35
   namespace = "com.matejdro.weardialer.common"

   defaultConfig {
      minSdk = 26
   }

   compileOptions {
      sourceCompatibility(JavaVersion.VERSION_21)
      targetCompatibility(JavaVersion.VERSION_21)
   }
}

kotlin {
   jvmToolchain(21)
}

wire {
   kotlin {}
}

dependencies {
   api(libs.wire.runtime)

   implementation(libs.androidx.compose.ui)

}
