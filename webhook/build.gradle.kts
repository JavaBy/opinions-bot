plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val intTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

configurations["intTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    implementation(project(":youtube"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.10.3"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.amazonaws:aws-lambda-java-events:2.2.7")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")
    implementation("com.amazonaws:aws-lambda-java-log4j2:1.1.0")
    implementation("org.koin:koin-core:2.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
    implementation("com.google.api-client:google-api-client:1.23.0")
    implementation("org.jsoup:jsoup:1.13.1")

    testImplementation(platform("org.junit:junit-bom:5.6.0"))
    testRuntimeOnly(platform("org.junit:junit-bom:5.6.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.apache.logging.log4j:log4j-core")
    testImplementation("io.mockk:mockk:1.10.0")
    intTestImplementation("by.dev.madhead.aws-junit5:dynamo-v2:5.0.4")
}
