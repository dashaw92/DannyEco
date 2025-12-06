val spigotVersion = "1.21.10-R0.1-SNAPSHOT"
val spigotApi = "1.21"

plugins {
    kotlin("jvm") version "2.2.20"
}

group = "me.danny"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigotVersion")
    compileOnly("com.github.milkbowl:VaultAPI:1.7")
}

kotlin {
    jvmToolchain(21)
}