plugins {
    kotlin("multiplatform")
}

group = "dev.eskt"

kotlin {
    setupPlatforms()

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":event-store:api"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
