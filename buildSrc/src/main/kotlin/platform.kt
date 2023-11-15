import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.setupPlatforms(jvm: Boolean = true, native: Boolean = true, node: Boolean = true) {
    // Enable the default target hierarchy
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.default()

    jvmToolchain(17)

    if (jvm) {
        jvm {
            withJava()
        }
    }

    if (node) {
        js(IR) {
            nodejs()
        }
    }

    if (native) {
        linuxX64()
        // waiting for Okio 3.6 release
        // linuxArm64()
        macosX64()
        macosArm64()
    }
}
