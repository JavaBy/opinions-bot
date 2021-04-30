plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api(project(":youtube"))
    api("org.jsoup:jsoup:1.13.1")
}
