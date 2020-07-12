import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import kr.entree.spigradle.data.Load

plugins {
    id("java")
    kotlin("jvm") version "1.3.72"

    id("kr.entree.spigradle") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"

    id("eclipse")
    // id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

group = "com.dumbdogdiner"
version = "2.1.1"

java {
    sourceCompatibility = JavaVersion.VERSION_12
    targetCompatibility = JavaVersion.VERSION_12
}

repositories {
    mavenCentral()
    jcenter()
    jitpack()
    codemc()

    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/central") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi") }
    maven { url = uri("https://raw.githubusercontent.com/JorelAli/1.13-Command-API/mvn-repo/1.13CommandAPI/") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")

    compileOnly(paper("1.16.1"))
    compileOnly(vault())
    compileOnly("me.clip:placeholderapi:2.10.6")

    implementation("org.jetbrains.exposed", "exposed-core", "0.26.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.26.1")
    implementation("pw.forst", "exposed-upsert", "1.0")
    implementation("org.postgresql", "postgresql", "42.2.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "12"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "12"
    }

    build {
        dependsOn("shadowJar")
    }

    task<ConfigureShadowRelocation>("relocateShadowJar") {
        target = shadowJar.get()
        prefix = project.name
    }

    shadowJar {
        // This is causing loads of problems for some reason.
        // dependsOn("relocateShadowJar")

        archiveClassifier.set("")

        project.configurations.implementation.configure { isCanBeResolved = true }
        project.configurations.shadow.configure { isCanBeResolved = true }

        configurations = listOf(
            project.configurations.implementation.get(),
            project.configurations.shadow.get()
        )
    }

    spigot {
        authors = listOf("Vlad Frangu")
        softDepends = listOf("Vault", "PlaceholderAPI")
        apiVersion = "1.16"
        load = Load.STARTUP
        loadBefore = listOf("ItemFrameShops")
        commands {
            create("balance") {
                aliases = listOf("gbal", "gmoney", "bal")
                description = "Shows your balances"
                usage = "/gbalance [player]"
            }
            create("baltop") {
                aliases = listOf("gmoneytop", "gecotop", "gtop", "gbaltop")
                description = "Shows the top balances of the server"
                usage = "/baltop [currency] [page]"
            }
            create("currency") {
                aliases = listOf("gcurr", "gcurrency", "currencies")
                description = "Manage the currencies."
                usage = "/gcurrencies <backend|color|colorlist|convert|create|decimals|default|delete|list|payable|setrate|startbal|symbol|view>"
            }
            create("economy") {
                aliases = listOf("geconomy", "eco", "geco", "gemseconomy")
                description = "Give, set or take currency."
                usage = "/eco <add|give|remove|set|take> <account> <amount> [currency]"
            }
            create("pay") {
                aliases = listOf("gpay")
                description = "Pays another account"
                usage = "/pay <account> <amount> [currency]"
            }
            create("check") {
                aliases = listOf("note", "banknote")
                description = "Write or Redeem a check"
                usage = "/check <redeem|write> [amount] [currency]"
            }
//          create("exchange") {
//              aliases = listOf("gex", "ex")
//              description = "Exchange currencies"
//              usage = "/exchange [player] <old currency> <amount> <new currency> [receive amount]"
//          }
        }
    }
}

// Eclipse classpath bugfix
eclipse {
    classpath {
        containers = setOf("org.eclipse.buildship.core.gradleclasspathcontainer")
    }
}
