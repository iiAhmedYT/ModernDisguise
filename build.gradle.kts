import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.8"
}

java {
    withSourcesJar()
    withJavadocJar()
}

allprojects {
    group = "dev.iiahmed"
    version = "4.1"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.codemc.org/repository/nms/")
    }
}

val versionsProject = project(":version")
dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")

    api(project(":ModernDisguise-API"))
    versionsProject.subprojects.forEach { subproject ->
        subproject.afterEvaluate {
            if (subproject.plugins.hasPlugin("dev.iiahmed.recraft")) {
                implementation(project(":version:${subproject.name}", configuration = "recraft"))
            } else {
                implementation(subproject)
            }
        }
    }
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

if (gradle.startParameter.taskNames.any { it.contains("publish", ignoreCase = true) }) {
    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
    }
}

tasks.register<Jar>("aggregateSources") {
    archiveClassifier.set("sources")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    subprojects.forEach { sub ->
        sub.plugins.withType<JavaPlugin> {
            val sourcesJar = sub.tasks.findByName("sourcesJar")
            val sourceSet = sub.extensions.findByType<SourceSetContainer>()?.findByName("main")

            if (sourcesJar != null && sourceSet != null) {
                dependsOn(sourcesJar)
                from(sourceSet.allSource)
            }
        }
    }
}

tasks.register<Jar>("aggregateJavadoc") {
    archiveClassifier.set("javadoc")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Configure dependencies during configuration phase
    subprojects.forEach { sub ->
        val javadocTask = sub.tasks.findByName("javadoc") as? Javadoc
        if (javadocTask != null) {
            dependsOn(javadocTask)
        }
    }

    // Delay adding sources until execution time
    doFirst {
        subprojects.forEach { sub ->
            val javadocTask = sub.tasks.findByName("javadoc") as? Javadoc
            if (javadocTask != null && javadocTask.destinationDir?.exists() == true) {
                from(javadocTask.destinationDir)
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("modernDisguise") {
            artifact(tasks.named("shadowJar"))

            artifact(tasks.named("aggregateSources").get())
            artifact(tasks.named("aggregateJavadoc").get())

            artifactId = "ModernDisguise"
            groupId = project.group.toString()
            version = project.version.toString()

            pom {
                name.set("ModernDisguise")
                description.set("Lightweight high quality library to help you add a disguise system to your Minecraft plugin")
                url.set("https://github.com/iiAhmedYT/ModernDisguise")

                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                        distribution.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                }

                developers {
                    developer {
                        id.set("iiahmedyt")
                        name.set("iiAhmedYT")
                        url.set("https://github.com/iiAhmedYT/")
                    }
                }

                scm {
                    url.set("https://github.com/iiAhmedYT/ModernDisguise/")
                    connection.set("scm:git:git://github.com/iiAhmedYT/ModernDisguise.git")
                    developerConnection.set("scm:git:ssh://git@github.com/iiAhmedYT/ModernDisguise.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GraveMC"
            url = uri("https://repo.gravemc.net/releases")
            credentials {
                username = findProperty("gravemc.repo.user") as String? ?: System.getenv("GRAVEMC_REPO_USER")
                password = findProperty("gravemc.repo.password") as String? ?: System.getenv("GRAVEMC_REPO_PASSWORD")
            }
        }
    }
}

tasks.named("generateMetadataFileForModernDisguisePublication") {
    dependsOn(tasks.named("aggregateSources"))
    dependsOn(tasks.named("aggregateJavadoc"))
}
