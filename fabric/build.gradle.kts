plugins {
    id("cobblemonmegas.platform-conventions")
}

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
    mavenLocal()
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
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

    modImplementation("com.cobblemon:fabric:${rootProject.property("cobblemon_version")}")

    modImplementation(libs.fabricLoader)
    modRuntimeOnly(libs.fabricApi)
    modImplementation(libs.fabricKotlin)
    modApi(libs.fabricPermissionsApi)
    modApi(libs.architecturyFabric)
}

tasks {
    // The AW file is needed in :fabric project resources when the game is run.
    val copyAccessWidener by registering(Copy::class) {
        from(loom.accessWidenerPath)
        into(generatedResources)
    }

    shadowJar {}

    processResources {
        inputs.property("version", rootProject.version)

        filesMatching("fabric.mod.json") {
            expand("version" to rootProject.version)
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:0.16.5")
    }
}
