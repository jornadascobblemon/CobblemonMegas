rootProject.name = "CobblemonMegas"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net/")
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}
plugins {
    id("ca.stellardrift.polyglot-version-catalogs") version "5.0.1"
}

listOf(
    "common",
    "fabric",
    // "forge"
).forEach { setupProject(it, file(it)) }

fun setupProject(name: String, projectDirectory: File) = setupProject(name) {
    projectDir = projectDirectory
}

inline fun setupProject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}

// https://github.com/jornadascobblemon/CobblemonMegas/issues/4
buildscript {
    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "com.google.code.gson" && requested.name == "gson") {
                    useVersion("2.10.1")
                    because("Fabric Loom requires a more recent version of Guava than FooJay toolchains")
                }
            }
        }
    }
}