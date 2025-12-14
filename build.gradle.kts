val spigotApi = "1.21"

plugins {
    kotlin("jvm") version "2.2.20"
}

group = "me.danny"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    implementation("com.github.milkbowl:VaultAPI:1.7") {
        exclude("org.bukkit")
    }
}


//configurations.all {
//    resolutionStrategy.capabilitiesResolution.withCapability("org.bukkit:bukkit") {
//        select("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
//    }
//}

kotlin {
    jvmToolchain(21)
}