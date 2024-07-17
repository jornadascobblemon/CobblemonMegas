plugins {
    base
    id("cobblemonmegas.root-conventions")
}

group = "com.selfdot.cobblemonmegas"
version = "${project.property("mod_version")}+${project.property("cobblemon_version")}"

val isSnapshot = project.property("snapshot")?.equals("true") ?: false
if (isSnapshot) {
    version = "$version-SNAPSHOT"
}