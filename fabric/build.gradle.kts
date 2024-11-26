import org.gradle.kotlin.dsl.modApi

architectury {
    platformSetupLoomIde()
    fabric()
}

val generatedResources = file("src/generated/resources")

sourceSets {
    main {
        resources {
            srcDir(generatedResources)
        }
    }
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
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

    // shadowJar {
    //     archiveClassifier.set("dev-shadow")
    //     archiveBaseName.set("CobblemonMegas-${project.name}")
    //     configurations = listOf(bundle)
    //     mergeServiceFiles()
    // }
    //
    // remapJar {
    //     dependsOn(shadowJar)
    //     inputFile.set(shadowJar.flatMap { it.archiveFile })
    //     archiveBaseName.set("CobblemonMegas-${project.name}")
    //     archiveVersion.set("${rootProject.version}")
    // }

}

dependencies {
    implementation(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    "developmentFabric"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    bundle(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    modImplementation("com.cobblemon:fabric:${rootProject.property("cobblemon_version")}+${rootProject.property("mc_version")}")
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_api_version")}")
    modApi("me.lucko:fabric-permissions-api:${rootProject.property("fabric_permissions_api_version")}")
    modApi("dev.architectury:architectury-fabric:${rootProject.property("architectury_version")}")
}
//
// tasks {
//     // The AW file is needed in :fabric project resources when the game is run.
//     val copyAccessWidener by registering(Copy::class) {
//         from(loom.accessWidenerPath)
//         into(generatedResources)
//     }
//
//     shadowJar {}
//
//     processResources {
//         inputs.property("version", rootProject.version)
//
//         filesMatching("fabric.mod.json") {
//             expand("version" to rootProject.version)
//         }
//     }
// }
//
// configurations.all {
//     resolutionStrategy {
//         force("net.fabricmc:fabric-loader:0.15.11")
//     }
// }
