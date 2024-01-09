import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("multiplatform")
    id("com.diffplug.spotless")
    id("maven-publish")
}

kotlin {
    explicitApi()
}

spotless {
    kotlin {
        target("**/src/**/*.kt")
        ktlint("1.1.1")
            .setEditorConfigPath("${rootProject.projectDir}/.editorconfig")
    }
    kotlinGradle {
        target("**/build.gradle.kts", "**/settings.gradle.kts")
        ktlint("1.1.1")
            .setEditorConfigPath("${rootProject.projectDir}/.editorconfig")
    }
}
