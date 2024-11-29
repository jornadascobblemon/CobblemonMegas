import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("dev.architectury.loom")
    id("architectury-plugin")
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://maven.impactdev.net/repository/development/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

architectury {
    minecraft = project.property("mc_version").toString()
}

loom {
    silentMojangMappingsLicense()
    enableTransitiveAccessWideners.set(true)

    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName.set("cobblemonmegas-${project.name}-refmap.json")
    }
}

dependencies {
    minecraft("net.minecraft:minecraft:${rootProject.property("mc_version")}")
    mappings("net.fabricmc:yarn:${rootProject.property("yarn_version")}")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

        tasks.withType<KotlinCompile> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }

    withType<Jar> {
        from(rootProject.file("LICENSE"))
    }
}