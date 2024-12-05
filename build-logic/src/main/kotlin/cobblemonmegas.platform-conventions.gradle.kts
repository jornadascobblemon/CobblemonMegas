plugins {
    id("cobblemonmegas.base-conventions")
    id("com.github.johnrengelman.shadow")
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
        archiveVersion.set("${rootProject.version}")
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("CobblemonMegas-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }

}