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
                api(project(":event-store:api"))
                implementation(project(":event-store:impl-common"))
                implementation(libs.okio.core)
                implementation(libs.kotlinx.serialization.cbor) // used for default metadata serializer only
                implementation(libs.kotlinx.serialization.protobuf)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":event-store:test-harness"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.okio.node)
            }
        }
    }
}

setupPublishing("event-store-fs")
