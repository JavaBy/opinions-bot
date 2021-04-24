plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":news-queue"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.10.3"))
    implementation("org.koin:koin-core:2.1.5")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.amazonaws:aws-lambda-java-events:2.2.7")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")
    implementation("com.amazonaws:aws-lambda-java-log4j2:1.1.0")
    implementation ("com.github.ivanisidrowu.KtRssReader:kotlin:v2.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
}
