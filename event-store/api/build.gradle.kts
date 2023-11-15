plugins {
    standardMultiplatformModule()
}

group = "dev.eskt"

kotlin {
    setupPlatforms()

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

setupCompiler()

setupPublishing("event-store-api")
