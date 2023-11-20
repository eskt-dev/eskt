plugins {
    standardMultiplatformModule()
    kotlin("plugin.serialization")
}

group = "dev.eskt"

kotlin {
    setupPlatforms()

    sourceSets {
        commonMain {
            dependencies {
                api(project(":event-store:api"))
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

setupCompiler()
