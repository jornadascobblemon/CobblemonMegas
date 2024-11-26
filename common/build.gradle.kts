architectury {
    common("${rootProject.property("enabled_platforms")}".split(","))
    platformSetupLoomIde()
}

loom {
    silentMojangMappingsLicense()

    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName.set("cobblemonmegas-${project.name}-refmap.json")
    }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    modImplementation("com.google.code.findbugs:jsr305:3.0.2")
    modApi("com.cobblemon:mod:${rootProject.property("cobblemon_version")}+${rootProject.property("mc_version")}")
    modApi("dev.architectury:architectury:${rootProject.property("architectury_version")}")

    compileOnly("net.luckperms:api:${rootProject.property("luckperms_version")}")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}
