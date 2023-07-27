plugins {
    standardMultiplatformModule()
    kotlin("plugin.serialization")
}

group = "dev.eskt"

kotlin {
    setupPlatforms()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":event-store:storage-api"))
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
