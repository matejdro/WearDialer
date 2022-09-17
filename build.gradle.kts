import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
   repositories {
      google()
      mavenCentral()
      gradlePluginPortal()
   }
   dependencies {
      classpath(libs.androidPluginGradle)
      classpath(libs.kotlin.pluginGradle)
      classpath(libs.wire.pluginGradle)
      classpath(libs.dagger.hilt.plugin)
      classpath(libs.versionsPlugin.gradle)
   }
}


allprojects {
   apply(plugin = "com.github.ben-manes.versions")

   afterEvaluate {
      tasks.withType<DependencyUpdatesTask> {
         rejectVersionIf {
            candidate.version.contains("alpha", ignoreCase = true) ||
                    candidate.version.contains("beta", ignoreCase = true) ||
                    candidate.version.contains("RC", ignoreCase = true) ||
                    candidate.version.contains("M", ignoreCase = true)
         }
      }
   }
}
