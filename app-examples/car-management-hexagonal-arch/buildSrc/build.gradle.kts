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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    implementation(kotlin("serialization", version = "1.9.22"))
}
