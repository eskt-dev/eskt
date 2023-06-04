dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    plugins {
        kotlin("multiplatform") version "1.8.21"
    }
}

rootProject.name = "eskt-parent"
