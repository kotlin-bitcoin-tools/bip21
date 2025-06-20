import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.gradle.maven-publish")
    id("org.jetbrains.dokka") version "2.0.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "org.kotlinbitcointools"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    explicitApi()
}

dependencies {
    implementation("io.ktor:ktor-client-core-jvm:2.3.13")
    implementation("com.ionspin.kotlin:bignum:0.3.8")
    implementation("com.eygraber:uri-kmp:0.0.14")

    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.0")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("bip21")
            description.set("A library to parse and generate BIP21 URIs.")
            url.set("https://github.com/kotlin-bitcoin-tools")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://github.com/kotlin-bitcoin-tools/bip21/blob/master/LICENSE.txt")
                }
            }
            developers {
                developer {
                    id.set("thunderbiscuit")
                    name.set("thunderbiscuit")
                    email.set("thunderbiscuit@protonmail.com")
                }
            }
            scm {
                connection.set("smc:git:https://github.com:kotlin-bitcoin-tools/bip21.git")
                developerConnection.set("smc:git:git@github.com:kotlin-bitcoin-tools/bip21.git")
                url.set("https://github.com/kotlin-bitcoin-tools/bip21")
            }
        }
    }
}

dokka {
    moduleName.set("bip21")
    moduleVersion.set("0.1.0-SNAPSHOT")
    dokkaSourceSets {
        named("main") {
            includes.from("MODULE.md")
            samples.from("src/test/kotlin/org/kotlinbitcointools/bip21/Samples.kt")
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl("https://kotlin-bitcoin-tools.github.io/bip21/")
                remoteLineSuffix.set("#L")
            }
        }
    }
    pluginsConfiguration.html {
        // customStyleSheets.from("styles.css")
        // customAssets.from("logo.svg")
        footerMessage.set("(c) Kotlin Bitcoin Tools Developers")
    }
}

ktlint {
    version = "1.5.0"
    ignoreFailures = false
    reporters {
        reporter(ReporterType.PLAIN).apply { outputToConsole = true }
    }
}
