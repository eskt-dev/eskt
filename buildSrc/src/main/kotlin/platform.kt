import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.setupPlatforms(jvm: Boolean = true, native: Boolean = true, node: Boolean = true) {
    // Enable the default target hierarchy
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.default()

    if (jvm) {
        jvm {
            jvmToolchain(17)
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
//        TODO restore after https://github.com/square/okio/pull/1301
//        linuxArm64()
        macosX64()
        macosArm64()
    }
}
