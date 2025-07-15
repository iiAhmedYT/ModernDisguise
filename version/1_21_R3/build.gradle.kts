plugins {
    java
    id("dev.iiahmed.recraft") version "1.0.0"
}

recraft {
    minecraftVersion.set("1.21.4")
    targetedPackages.set(listOf("dev/iiahmed/disguise/vs"))
    jarFilePattern = "libs/${project.name}-${project.version}.jar"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly(project(":ModernDisguise-API"))
    compileOnly("org.jetbrains:annotations:24.1.0")
}
