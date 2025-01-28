plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.0"
    id("org.gradle.maven-publish")
    id("org.jetbrains.dokka") version "2.0.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "org.kotlinbitcointools"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// testing {
//     suites {
//         // Configure the built-in test suite
//         val test by getting(JvmTestSuite::class) {
//             // Use Kotlin Test test framework
//             useKotlinTest("1.9.0")
//         }
//     }
// }

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    iosArm64()          // iPhone and iPad, bip21-iosarm64
    iosX64()            // x86_64 simulator (Intel-based macs), bip21-iosx64
    iosSimulatorArm64() // arm64 simulator (Apple Silicon macs), bip21-iossimulatorarm64

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:2.0.0")
                implementation("com.ionspin.kotlin:bignum:0.3.8")
                implementation("com.eygraber:uri-kmp:0.0.14")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
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
    // publications {
    //     create<MavenPublication>("Maven") {
    //         groupId = "org.kotlinbitcointools"
    //         artifactId = "bip21"
    //         version = version
    //
    //         from(components["kotlin"])
    //     }
    // }
}

nexusPublishing {
    repositories {
        create("sonatype") {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

            val ossrhUsername: String? by project
            val ossrhPassword: String? by project
            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}

dokka {
    moduleName.set("bip21")
    moduleVersion.set("0.1.0-SNAPSHOT")
    dokkaSourceSets.commonMain {
        includes.from("MODULE.md")
        samples.from("src/commonTest/kotlin/org/kotlinbitcointools/bip21/Samples.kt")
        sourceLink {
            localDirectory.set(file("src/commonMain/kotlin"))
            remoteUrl("https://kotlin-bitcoin-tools.github.io/bip21/")
            remoteLineSuffix.set("#L")
        }
    }
    pluginsConfiguration.html {
        // customStyleSheets.from("styles.css")
        // customAssets.from("logo.svg")
        footerMessage.set("(c) Kotlin Bitcoin Tools Developers")
    }
}
