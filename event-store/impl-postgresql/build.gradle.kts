plugins {
    standardMultiplatformModule()
}

group = "dev.eskt"

kotlin {
    setupPlatforms(jvm = true, native = false, node = false)

    sourceSets {
        commonMain {
            dependencies {
                api(project(":event-store:api"))
                implementation(project(":event-store:impl-common"))
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":event-store:test-harness"))
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.postgresql.jdbc)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.hikaricp)
            }
        }
    }
}

setupCompiler()

setupPublishing("event-store-impl-postgresql")
