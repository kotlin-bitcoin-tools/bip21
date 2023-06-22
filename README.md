# Readme
⚠️This library is not currently production-ready. Use at your own risk. ⚠️

This library is an implementation of the [BIP-0021] specification. It is written in Kotlin and is intended to be used in Kotlin Multiplatform projects. Please help us review it and make it production-ready! The API is still in flux, and we are open to suggestions. See the [issues] for discussion items and design decisions.

The main goals of this library are:
- [x] 1. BIP-0021 compliant
- [ ] 2. Well tested
- [ ] 3. Well documented
- [ ] 4. Production ready
- [ ] 5. Usable in KMP projects (JVM and iOS platforms)

The library is not currently available on Maven Central. To build locally and deploy to your local Maven repository, see the [build instructions](#build-instructions).

## Build Instructions
To build the library locally and deploy to your local Maven repository, run the following command:
```shell
./gradlew publishToMavenLocal
```

The library will be available in your local Maven repository (typically at `~/.m2/repository/` for macOS and Linux systems) under the group ID `org.bitcointools` and the artifact ID `bip21`. You can import it in your project as you would any other Maven dependency, provided you have your local Maven repository (`mavenLocal()`) configured as a dependency source:
```kotlin
// root-level build.gradle.kts
allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

// app-level build.gradle.kts
implementation("org.bitcointools:bip21:0.0.1")
```

[BIP-0021]: https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki
[issues]: 