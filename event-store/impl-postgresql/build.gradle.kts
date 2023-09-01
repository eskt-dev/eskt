plugins {
    standardMultiplatformModule()
}

group = "dev.eskt"

kotlin {
    setupPlatforms(jvm = true, native = false, node = false)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":event-store:api"))
                implementation(project(":event-store:impl-common"))
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":event-store:test-harness"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.postgresql.jdbc)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.hikaricp)
            }
        }
    }
}

setupPublishing("event-store-postgresql")
