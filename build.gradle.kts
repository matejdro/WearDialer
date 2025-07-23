import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import nl.littlerobots.vcu.plugin.resolver.ModuleVersionCandidate
import nl.littlerobots.vcu.plugin.versionSelector

plugins {
   alias(libs.plugins.versionCatalogUpdate)
}

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


versionCatalogUpdate {
   catalogFile.set(file("libs.toml"))

   fun ModuleVersionCandidate.newlyContains(keyword: String): Boolean {
      return !currentVersion.contains(keyword, ignoreCase = true) && candidate.version.contains(keyword, ignoreCase = true)
   }

   versionSelector {
      !it.newlyContains("alpha") &&
              !it.newlyContains("beta") &&
              !it.newlyContains("RC") &&
              !it.newlyContains("M") &&
              !it.newlyContains("eap") &&
              !it.newlyContains("dev") &&
              !it.newlyContains("pre")
   }
}

// Always update to the ALL distribution when updating Gradle
tasks.wrapper {
   distributionType = Wrapper.DistributionType.ALL
}
