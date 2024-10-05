plugins {
    standardMultiplatformModule()
}

group = "dev.eskt"

kotlin {
    setupPlatforms(ios = false, node = false)

    sourceSets {
        commonMain {
            dependencies {
                api(project(":event-store:api"))
                api(project(":hex-arch-ports"))
                api(project(":hex-arch-adapters-common"))

//                implementation(libs.slf4j.api)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

setupPublishing("hex-arch-adapters-ktor")
