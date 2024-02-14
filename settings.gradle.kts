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
      create("wearUtilsLibs") {
         from(files("wearutils/libs.toml"))
      }
   }
}

rootProject.name = "WearDialer"
include(":mobile")
include(":common")
include(":wear")
include(":wearutils")
