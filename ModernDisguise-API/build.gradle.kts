plugins {
    `java-library`
}

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.codemc.org/repository/nms/")
    }
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:24.1.0")
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(8)
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:unchecked",
            "-Xlint:deprecation",
            "-parameters"
        )
    )
}
