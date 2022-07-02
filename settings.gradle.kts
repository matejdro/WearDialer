enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
   }
   versionCatalogs {
      create("libs") {
         from(files("libs.toml"))
      }
   }
}

rootProject.name = "WearDialer"
include(":mobile")
include(":common")
include(":wear")
