plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":voting"))
    api("com.google.apis:google-api-services-youtube:v3-rev222-1.25.0")
}
