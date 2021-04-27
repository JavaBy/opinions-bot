plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":voting"))
    api(project(":news-queue"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
}
