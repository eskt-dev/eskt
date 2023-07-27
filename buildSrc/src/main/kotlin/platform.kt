import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.setupPlatforms() {
    // Enable the default target hierarchy
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.default()

    jvm {
        jvmToolchain(17)
        withJava()
    }

    js(IR) {
        nodejs()
    }

    linuxX64()
//    TODO restore after https://github.com/square/okio/pull/1301
//    linuxArm64()
    macosX64()
    macosArm64()
}
