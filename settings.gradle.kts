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
include(":event-store:storage-api")
include(":event-store:test-harness")
