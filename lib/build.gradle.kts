plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.8.10"
}

val libraryVersion: String by project

repositories {
    mavenCentral()
}

dependencies {}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest("1.8.10")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    explicitApi()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.bitcointools"
            artifactId = "bip21"
            version = libraryVersion

            from(components["java"])
        }
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set("bip21")
            moduleVersion.set(libraryVersion)
            // includes.from("Module.md")
            // samples.from("src/test/kotlin/org/bitcointools/Samples.kt")
        }
    }
}
