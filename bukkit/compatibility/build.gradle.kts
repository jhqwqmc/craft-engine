repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://r.irepo.space/maven/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // papi
    maven("https://maven.enginehub.org/repo/") // worldguard worldedit
    maven("https://repo.rapture.pw/repository/maven-releases/")  // slime world
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")  // slime world
    maven("https://repo.momirealms.net/releases/")
    maven("https://mvn.lumine.io/repository/maven-public/") // model engine mythic mobs
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/") // mmoitems
    maven("https://repo.viaversion.com") // via
    maven("https://repo.skriptlang.org/releases/") // skript
    maven("https://nexus.neetgames.com/repository/maven-releases/") // mcmmo
    maven("https://repo.dmulloy2.net/repository/public/") // mcmmo required
    maven("https://repo.auxilor.io/repository/maven-public/") // eco
    maven("https://repo.hiusers.com/releases") // zaphkiel
    maven("https://jitpack.io") // sxitem slimefun
    maven("https://repo.codemc.io/repository/maven-public/") // quickshop
    maven("https://repo.nexomc.com/releases/") // nexo
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":bukkit"))
    compileOnly(project(":bukkit:compatibility:legacy"))
    compileOnly("net.momirealms:sparrow-nbt:${rootProject.properties["sparrow_nbt_version"]}")
    // NMS
    compileOnly("net.momirealms:craft-engine-nms-helper:${rootProject.properties["nms_helper_version"]}")
    // Platform
    compileOnly("io.papermc.paper:paper-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")
    // NeigeItems
    compileOnly("pers.neige.neigeitems:NeigeItems:1.21.42")
    // Placeholder
    compileOnly("me.clip:placeholderapi:${rootProject.properties["placeholder_api_version"]}")
    // SlimeWorld
    compileOnly("com.infernalsuite.asp:api:4.0.0-SNAPSHOT")
    // ModelEngine
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.8")
    // BetterModel
    compileOnly("io.github.toxicity188:bettermodel:1.14.0")
    compileOnly("com.mojang:authlib:${rootProject.properties["authlib_version"]}")
    // MMOItems
    compileOnly("net.Indyuce:MMOItems-API:6.10-SNAPSHOT")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    // Nexo
    compileOnly("com.nexomc:nexo:1.13.0")
    // LuckPerms
    compileOnly("net.luckperms:api:5.4")
    // viaversion
    compileOnly("com.viaversion:viaversion-api:5.3.2")
    // Skript
    compileOnly("com.github.SkriptLang:Skript:2.11.0")
    // AuraSkills
    compileOnly("dev.aurelium:auraskills-api-bukkit:2.2.4")
    // FAWE
    compileOnly(platform("com.intellectualsites.bom:bom-newest:1.52"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
    // MythicMobs
    compileOnly("io.lumine:Mythic-Dist:5.9.0")
    // McMMO
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.2.038")
    // MMOCore
    compileOnly("net.Indyuce:MMOCore-API:1.12.1-SNAPSHOT")
    // JobsReborn
    compileOnly("com.github.Zrips:Jobs:v5.2.2.3")
    // CustomFishing
    compileOnly("net.momirealms:custom-fishing:2.3.3")
    // CustomNameplates
    compileOnly("net.momirealms:custom-nameplates:3.0.33")
    // eco
    compileOnly("com.willfp:eco:6.70.1")
    compileOnly("com.willfp:EcoJobs:3.56.1")
    compileOnly("com.willfp:EcoSkills:3.46.1")
    compileOnly("com.willfp:libreforge:4.58.1")
    // AureliumSkills
    compileOnly("com.github.Archy-X:AureliumSkills:Beta1.3.21")
    // Zaphkiel
    compileOnly("ink.ptms:ZaphkielAPI:2.1.0")
    // WorldGuard
    compileOnly(files("${rootProject.rootDir}/libs/worldguard-bukkit-7.0.14-dist.jar"))
    // HeadDatabase
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    // SXItem
    compileOnly("com.github.Saukiya:SX-Item:4.4.6")
    // Slimefun
    compileOnly("io.github.Slimefun:Slimefun4:RC-32")
    // QuickShop
    compileOnly("com.ghostchu:quickshop-api:6.2.0.10")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}