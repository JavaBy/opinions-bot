plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":components"))
    implementation("com.github.ivanisidrowu.KtRssReader:kotlin:v2.1.1")
}