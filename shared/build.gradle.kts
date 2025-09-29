plugins {
    kotlin("jvm") version "1.9.20"
}

group = "dev.yidafu.face.detection"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    implementation(libs.multik.core)
    implementation(libs.multik.default)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}