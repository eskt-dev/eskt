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
                api(project(":hex-arch-ports"))
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}

setupPublishing("hex-arch-adapters-common")
