plugins {
    base
    id("cobblemonmegas.root-conventions")
}

base.archivesName.set(rootProject.property("archives_base_name").toString())
group = rootProject.property("maven_group").toString()
version = "${project.property("mod_version")}-${project.property("cobblemon_version")}-${project.property("minecraft_version")}"

val isSnapshot = project.property("snapshot")?.equals("true") ?: false
if (isSnapshot) {
    version = "$version-SNAPSHOT"
}