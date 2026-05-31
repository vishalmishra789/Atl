plugins {
    // Standard Loom plugin for Fabric
    id("net.fabricmc.loom") version "1.7-SNAPSHOT"
    id("io.freefair.lombok") version "8.6"
}

version = "${project.property("mod_version")}+mc${project.property("minecraft_version")}"
group = project.property("maven_group") as String

repositories {
    maven {
        url = uri("https://maven.uku3lig.net/releases")
    }
    maven {
        url = uri("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    }
    // Added for Mod Menu
    maven { 
        url = uri("https://maven.terraformersmc.com/releases/") 
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    modImplementation(fabricApi.module("fabric-command-api-v2", project.property("fabric_api_version") as String))

    modApi("net.uku3lig:ukulib:${project.property("ukulib_version")}")

    // Needed to compile your ModMenu settings button code
    modImplementation("com.terraformersmc:modmenu:13.0.4")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:${project.property("devauth_version")}")
}

base {
    archivesName = project.property("archives_base_name") as String
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
