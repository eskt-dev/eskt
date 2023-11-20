dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

rootProject.name = "car-management-hexagonal-arch"

include(":domain")
include(":spring-webflux-r2dbc-application")
