import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
}

repositories {
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
    mavenCentral()
}

dependencies {
    // Platform
    compileOnly("io.papermc.paper:paper-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")

    implementation(project(":core"))
    implementation(project(":bukkit"))
    implementation(project(":bukkit:legacy"))
    implementation(project(":bukkit:compatibility"))
    implementation(project(":bukkit:compatibility:legacy"))
    implementation(project(":common-files"))

    implementation("com.saicone.rtag:rtag-item:${rootProject.properties["rtag_version"]}")
    implementation("net.momirealms:sparrow-util:${rootProject.properties["sparrow_util_version"]}")
    implementation("net.momirealms:antigrieflib:${rootProject.properties["anti_grief_version"]}")
    implementation("net.momirealms:craft-engine-nms-helper-mojmap:${rootProject.properties["nms_helper_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}

paper {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "net.momirealms.craftengine.bukkit.plugin.PaperCraftEnginePlugin"
    bootstrapper = "net.momirealms.craftengine.bukkit.plugin.PaperCraftEngineBootstrap"
    version = rootProject.properties["project_version"] as String
    name = "CraftEngine"
    apiVersion = "1.20"
    authors = listOf("XiaoMoMi")
    contributors = listOf("jhqwqmc", "iqtesterrr")
    foliaSupported = true
    serverDependencies {
        register("PlaceholderAPI") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("WorldEdit") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("FastAsyncWorldEdit") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = false
        }
        register("Skript") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("NeigeItems") {
            required = false
        }
        register("MMOItems") {
            required = false
        }
        register("ModelEngine") {
            required = false
        }
        register("BetterModel") {
            required = false
        }
        register("AuraSkills") {
            required = false
        }
        register("LuckPerms") {
            required = false
        }
        register("ViaVersion") {
            required = false
        }
    }
}

artifacts {
    archives(tasks.shadowJar)
}

tasks {
    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
        archiveFileName = "${rootProject.name}-paper-plugin-${rootProject.properties["project_version"]}.jar"
        destinationDirectory.set(file("$rootDir/target"))
        relocate("net.kyori", "net.momirealms.craftengine.libraries")
        relocate("net.momirealms.sparrow.nbt", "net.momirealms.craftengine.libraries.nbt")
        relocate("net.momirealms.antigrieflib", "net.momirealms.craftengine.libraries.antigrieflib")
        relocate("com.saicone.rtag", "net.momirealms.craftengine.libraries.tag")
        relocate("org.incendo", "net.momirealms.craftengine.libraries")
        relocate("dev.dejvokep", "net.momirealms.craftengine.libraries")
        relocate("org.bstats", "net.momirealms.craftengine.libraries.bstats")
        relocate("com.github.benmanes.caffeine", "net.momirealms.craftengine.libraries.caffeine")
        relocate("com.ezylang.evalex", "net.momirealms.craftengine.libraries.evalex")
        relocate("net.bytebuddy", "net.momirealms.craftengine.libraries.bytebuddy")
        relocate("org.yaml.snakeyaml", "net.momirealms.craftengine.libraries.snakeyaml")
        relocate("org.ahocorasick", "net.momirealms.craftengine.libraries.ahocorasick")
        relocate("net.jpountz", "net.momirealms.craftengine.libraries.jpountz")
        relocate("software.amazon.awssdk", "net.momirealms.craftengine.libraries.awssdk")
        relocate("software.amazon.eventstream", "net.momirealms.craftengine.libraries.eventstream")
        relocate("com.google.common.jimfs", "net.momirealms.craftengine.libraries.jimfs")
        relocate("org.apache.commons", "net.momirealms.craftengine.libraries.commons")
    }
}
