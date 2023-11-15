plugins {
    standardMultiplatformModule()
}

group = "dev.eskt"

kotlin {
    setupPlatforms()

    sourceSets {
        commonMain {
            dependencies {
                api(project(":event-store:api"))
                implementation(project(":event-store:impl-common"))
                implementation(libs.kotlinx.atomicfu)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":event-store:test-harness"))
            }
        }
    }
}

setupCompiler()

setupPublishing("event-store-impl-memory")
