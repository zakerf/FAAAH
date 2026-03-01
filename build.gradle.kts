plugins {
    id("java")
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = "nl.rekaz"
version = "1.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.3.3")
        bundledPlugin("com.intellij.java")
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "nl.rekaz.faaah"
        name = "FAAAH Test Sound Player"
        description = "FAAAH plays customizable sounds when your test suites pass or fail"
        vendor {
            name = "rekaz"
        }
    }
}

kotlin {
    jvmToolchain(21)
}
