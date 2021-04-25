plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":news-queue"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
}
