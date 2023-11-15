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
            }
        }
    }
}

setupCompiler()

setupPublishing("event-store-impl-common")
