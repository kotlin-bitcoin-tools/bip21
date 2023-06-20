plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("fr.acinq.bitcoin:bitcoin-kmp-jvm:0.12.0")
    implementation("fr.acinq.secp256k1:secp256k1-kmp-jni-jvm-darwin:0.10.0")
}

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
