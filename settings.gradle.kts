dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    plugins {

    }
}

rootProject.name = "eskt-parent"

include(":event-store:api")
include(":event-store:memory")
