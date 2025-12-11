rootProject.name = "ModernDisguise"

include("version")
include("ModernExample")
include("ModernDisguise-API")

val minecraftVersions = listOf(
        "1_8_R3",
        "1_9_R2",
        "1_10_R1",
        "1_11_R1",
        "1_12_R1",
        "1_13_R1",
        "1_13_R2",
        "1_14_R1",
        "1_15_R1",
        "1_16_R1",
        "1_16_R2",
        "1_16_R3",
        "1_17_R1",
        "1_18_R1",
        "1_18_R2",
        "1_19_R1",
        "1_19_R2",
        "1_19_R3",
        "1_20_R1",
        "1_20_R2",
        "1_20_R3",
        "1_20_R4",
        "1_21_R1",
        "1_21_R2",
        "1_21_R3",
        "1_21_R4",
        "1_21_R5",
        "1_21_R6",
        "1_21_R7",
        "fallback"
)

minecraftVersions.forEach { version ->
    include("version:$version")
}

pluginManagement {
        repositories {
                mavenCentral()
                gradlePluginPortal()
                maven {
                    url = uri("https://repo.gravemc.net/releases/")
                }
        }
}
