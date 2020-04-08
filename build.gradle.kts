import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm").version("1.3.71")
    id("com.github.johnrengelman.shadow").version("5.2.0")
}

repositories {
    jcenter()
    maven("https://dl.bintray.com/madhead/maven")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.13.1"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.10.3"))
    implementation(platform("software.amazon.awssdk:bom:2.11.9"))
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")
    implementation("com.amazonaws:aws-lambda-java-events:2.2.7")
    implementation("com.amazonaws:aws-lambda-java-log4j2:1.1.0")
    implementation("by.dev.madhead.telek:telek-hc:0.0.6.ALPHA")
    implementation("org.koin:koin-core:2.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    implementation("software.amazon.awssdk:dynamodb")

    testImplementation(platform("org.junit:junit-bom:5.6.0"))
    testRuntimeOnly(platform("org.junit:junit-bom:5.6.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.apache.logging.log4j:log4j-core")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    val shadowJar by getting(ShadowJar::class) {
        transform(Log4j2PluginsCacheFileTransformer::class.java)
    }
}
