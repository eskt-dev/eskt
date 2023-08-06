plugins {
    standardMultiplatformModule()
}

group = "dev.eskt"

kotlin {
    setupPlatforms()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":event-store:api"))
            }
        }
    }
}

setupPublishing("event-store-impl-common")
