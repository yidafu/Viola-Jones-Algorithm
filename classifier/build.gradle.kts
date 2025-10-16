plugins {
    kotlin("jvm") version "2.2.20"
    application
}

group = "dev.yidafu.face.detection"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Multik for array operations
    implementation("org.jetbrains.kotlinx:multik-core:0.2.3")
    implementation("org.jetbrains.kotlinx:multik-default:0.2.3")
    
    testImplementation(kotlin("test"))
}

// Configure application entry point
application {
    mainClass.set("dev.yidafu.face.detection.DetectorAppKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}