plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.10.3"))
    implementation(platform("software.amazon.awssdk:bom:2.11.9"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.13.1"))

    implementation("org.koin:koin-core:2.1.5")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.amazonaws:aws-lambda-java-events:2.2.7")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("com.amazonaws:aws-lambda-java-log4j2:1.1.0")
    implementation ("com.github.ivanisidrowu.KtRssReader:kotlin:v2.1.1")
    implementation("software.amazon.awssdk:dynamodb")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
    implementation("com.github.insanusmokrassar:TelegramBotAPI:0.28.0")
}
