import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm").version(Versions.Plugins.KOTLIN).apply(false)
    kotlin("plugin.serialization").version(Versions.Plugins.KOTLIN).apply(false)
    id("com.github.johnrengelman.shadow").version(Versions.Plugins.SHADOW).apply(false)
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = Versions.JVM
        }
        withType<Jar> {
            // Workaround for https://stackoverflow.com/q/42174572/750510
            archiveBaseName.set(rootProject.name + "-" + this.project.path.removePrefix(":").replace(":", "-"))
        }
        withType<Test> {
            useJUnitPlatform()
            testLogging {
                showStandardStreams = true
            }
        }
        withType<ShadowJar> {
            transform(Log4j2PluginsCacheFileTransformer::class.java)
        }
    }
}
