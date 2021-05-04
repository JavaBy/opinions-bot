plugins {
    kotlin("jvm")
    `java-library`
}

group = "pl.domyno"
version = "1.0.0"

repositories {
    jcenter()
    mavenCentral()
}

val junitVersion = "5.6.2"

dependencies {
    api(project(":components"))
    implementation("org.dom4j:dom4j:2.1.3")

    api("com.github.ivanisidrowu.KtRssReader:kotlin:v2.1.1")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", junitVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
}

tasks {
    test {
        useJUnitPlatform()
    }
}