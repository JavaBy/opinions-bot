plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":youtube"))
    api(project(":news-queue"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
}
