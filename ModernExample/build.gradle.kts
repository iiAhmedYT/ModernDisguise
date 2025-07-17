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
    implementation("com.github.Revxrsal.Lamp:common:3.1.9")
    implementation("com.github.Revxrsal.Lamp:bukkit:3.1.9")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT")
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project(":").tasks.named("shadowJar"))
    archiveClassifier.set("")

    archiveFileName.set("moderndisguise-test-plugin-${project.version}.jar")
}