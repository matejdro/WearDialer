plugins {
   id("com.android.library")
   id("kotlin-android")
   id("com.squareup.wire")
}

android {
   compileSdk = 32

   defaultConfig {
      minSdk = 26
      targetSdk = 32
   }

   compileOptions {
      sourceCompatibility(JavaVersion.VERSION_1_8)
      targetCompatibility(JavaVersion.VERSION_1_8)
   }

   kotlinOptions {
      jvmTarget = "1.8"
   }
}

wire {
   kotlin {}
}

dependencies {
   api(libs.wire.runtime)

   implementation(libs.androidx.compose.ui)

}
