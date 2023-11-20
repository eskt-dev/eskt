repositories {
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    implementation(kotlin("serialization", version = "1.9.20"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.19.0")
}
