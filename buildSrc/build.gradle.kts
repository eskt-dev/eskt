repositories {
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
    implementation(kotlin("serialization", version = "1.9.10"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.19.0")
}
