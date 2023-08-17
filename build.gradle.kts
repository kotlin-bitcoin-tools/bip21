plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.9.0"
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.0"
}

val libraryVersion: String by project

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
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    ios()

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
        named("commonMain") {
            moduleName.set("bip21")
            moduleVersion.set(libraryVersion)
            // includes.from("Module.md")
            // samples.from("src/test/kotlin/org/bitcointools/bip21/Samples.kt")
        }
    }
}
