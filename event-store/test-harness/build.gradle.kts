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
                api(project(":event-store:impl-common"))
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(libs.kotlinx.serialization.json)
                implementation(kotlin("test"))
            }
        }
        jvmMain {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        jsMain {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

setupCompiler()
