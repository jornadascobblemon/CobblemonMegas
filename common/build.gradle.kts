plugins {
    id("cobblemonmegas.base-conventions")
}

architectury {
    common(
        "fabric",
        // "forge"
    )
}

repositories {
    maven {
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    mavenLocal()
    mavenCentral()
}

dependencies {
    modImplementation(libs.fabricLoader)
    modImplementation("com.google.code.findbugs:jsr305:3.0.2")
    modCompileOnly("com.cobblemon:mod:${rootProject.property("cobblemon_version")}") {
        isTransitive = false
        isChanging = true
    }
    modApi(libs.architectury)

    compileOnly("net.luckperms:api:${rootProject.property("luckperms_version")}")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}