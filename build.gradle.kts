val spigotVersion = "1.21.10-R0.1-SNAPSHOT"
val spigotApi = "1.21"

plugins {
    kotlin("jvm") version "2.2.20"
}

group = "me.danny"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
}