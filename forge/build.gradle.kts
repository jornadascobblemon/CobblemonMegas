plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    forge {
        mixinConfig("mixins.cobblemonmegas-common.json")
    }
}

val bundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

tasks {
    jar {
        archiveBaseName.set("CobblemonMegas-${project.name}")
        archiveClassifier.set("dev-slim")
    }

    shadowJar {
        archiveClassifier.set("dev-shadow")
        archiveBaseName.set("CobblemonMegas-${project.name}")
        configurations = listOf(bundle)
        mergeServiceFiles()
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("CobblemonMegas-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }
}


repositories {
    maven(url = "https://thedarkcolour.github.io/KotlinForForge/")
}

dependencies {
    implementation(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    "developmentForge"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    bundle(project(path = ":common", configuration = "transformProductionForge")) {
        isTransitive = false
    }

    forge("net.minecraftforge:forge:${rootProject.property("forge_version")}")
    modApi("dev.architectury:architectury-forge:${rootProject.property("architectury_version")}")
    implementation("thedarkcolour:kotlinforforge:${rootProject.property("kotlinforforge_version")}")
    testImplementation(project(":common", configuration = "namedElements"))
    modCompileOnly("com.cobblemon:forge:${rootProject.property("cobblemon_version")}+${rootProject.property("mc_version")}")
}

tasks {
    shadowJar {
        exclude("architectury-common.accessWidener")
        relocate ("com.ibm.icu", "com.cobblemon.mod.relocations.ibm.icu")
    }

    processResources {
        inputs.property("version", rootProject.version)

        filesMatching("META-INF/mods.toml") {
            expand("version" to rootProject.version)
        }
    }
}