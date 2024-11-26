import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("idea")
    kotlin("jvm") version "2.0.21"

    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT"
}

var fullVersionName = "${project.property("mod_version")}+${project.property("cobblemon_version")}"
val isSnapshot: Boolean = (property("snapshot")?.toString() == "true")
if (isSnapshot) {
    fullVersionName = "$fullVersionName-SNAPSHOT"
}

allprojects {
    group = "com.selfdot.cobblemonmegas"
    version = fullVersionName
}

architectury {
    minecraft = "${property("mc_version")}"
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")

    base.archivesName.set(fullVersionName + "-${project.name}")

    repositories {
        mavenCentral()
        maven("https://maven.impactdev.net/repository/development/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://files.minecraftforge.net/maven/")
    }

    dependencies {
        "minecraft"("com.mojang:minecraft:${property("mc_version")}")
        "mappings"("net.fabricmc:yarn:${property("yarn_version")}")
    }

    java {
        // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
        // if it is present.
        // If you remove this line, sources will not be generated.
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    tasks.withType<Jar> {
        from(rootProject.file("LICENSE"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Root assemble task
tasks.register<Copy>("collectJars") {
    val remapJars = subprojects.filter { it.name != "common" }.flatMap { it.tasks.matching { it.name == "remapJar" } }
    dependsOn(remapJars)

    from(remapJars.map { it.outputs.files })
    into(layout.buildDirectory.dir("libs"))
}

tasks.named("assemble") {
    dependsOn("collectJars")
}