import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
   java
    id("com.gradleup.shadow") version "8.3.8"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.helpch.at/releases")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/nms/")
    }
}

dependencies {
    implementation(project(":"))
    implementation("dev.velix:imperat-core:1.9.6")
    implementation("dev.velix:imperat-bukkit:1.9.6")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}


tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project(":").tasks.named("shadowJar"))
    archiveClassifier.set("")
    relocate("dev.velix.imperat", "dev.iiahmed.mexample.shade")
    archiveFileName.set("moderndisguise-test-plugin-${project.version}.jar")
}