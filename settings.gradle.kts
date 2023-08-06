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
include(":event-store:impl-common")

include(":event-store:impl-memory")
include(":event-store:impl-fs")

include(":event-store:test-harness")
