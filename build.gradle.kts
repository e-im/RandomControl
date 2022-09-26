import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "1.3.8"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "one.eim"
version = "1.0.0"
description = "Liberate your server from the RNG loving bourgeoisie! Paper plugin to enable RNG manipulation."

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    paperDevBundle("1.19.2-R0.1-SNAPSHOT")
    implementation("xyz.jpenilla", "reflection-remapper", "0.1.0-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }

    val autoReloc by register("configureShadowRelocation", ConfigureShadowRelocation::class) {
        target = shadowJar.get()
        val packageName = "${project.group}.${project.name.toLowerCase()}"
        prefix = "$packageName.lib"
    }

    shadowJar {
        minimize()
        dependsOn(autoReloc)
    }

    runServer {
        minecraftVersion("1.19.1")
        systemProperty("Paper.IgnoreJavaVersion", "true")
    }
}

bukkit {
    website = "https://github.com/e-im/RandomCOntrol"
    authors = listOf("e-im")
    main = "one.eim.randomcontrol.RandomControl"
    apiVersion = "1.17"
}
