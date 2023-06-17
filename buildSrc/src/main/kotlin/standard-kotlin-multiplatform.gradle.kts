import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("multiplatform")
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        target("**/src/**/*.kt")
        ktlint()
            .setEditorConfigPath("${rootProject.projectDir}/.editorconfig")
    }
    kotlinGradle {
        target("**/build.gradle.kts", "**/settings.gradle.kts")
        ktlint()
            .setEditorConfigPath("${rootProject.projectDir}/.editorconfig")
    }
}
