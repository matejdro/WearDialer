plugins {
   id("com.android.library")
   id("kotlin-android")
   id("com.squareup.wire")
}

android {
   compileSdk = 34
   namespace = "com.matejdro.weardialer.common"

   defaultConfig {
      minSdk = 26
   }

   compileOptions {
      sourceCompatibility(JavaVersion.VERSION_17)
      targetCompatibility(JavaVersion.VERSION_17)
   }
}

kotlin {
   jvmToolchain(17)
}

wire {
   kotlin {}
}

dependencies {
   api(libs.wire.runtime)

   implementation(libs.androidx.compose.ui)

}
