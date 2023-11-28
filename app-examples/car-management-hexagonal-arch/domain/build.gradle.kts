plugins {
	kotlin("multiplatform")
}

group = "dev.eskt.example"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(17)

    jvm()
    linuxX64()
//    linuxArm64()
    macosX64()
    macosArm64()

    sourceSets {
        commonMain {
            dependencies {
                api("dev.eskt:api")
                api("dev.eskt:hex-arch-ports")
                api("dev.eskt:test-harness-model")
                api("com.benasher44:uuid:0.8.2")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
