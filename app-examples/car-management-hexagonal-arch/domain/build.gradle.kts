plugins {
	kotlin("multiplatform")
}

group = "dev.eskt.example"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(17)

    jvm()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()

    sourceSets {
        commonMain {
            dependencies {
            }
        }
    }
}
