# Readme

⚠️This library is not currently production-ready. Use at your own risk. ⚠️
<br/>

This library is an implementation of the [BIP-0021] specification. Please help us review it and make it production-ready! The API is still in flux, and we are open to suggestions. See the [issues] for discussion items and design decisions.

The main goals of this library are:
- [x] 1. BIP-0021 compliant
- [x] 2. Well tested
- [ ] 3. Well documented
- [ ] 4. Production ready

## Install

The library is available on [Maven Central] under the group ID `org.kotlinbitcointools` and the artifact ID `bip21`:

```kotlin
// build.gradle.kts
implementation("org.kotlinbitcointools:bip21:0.1.0")
```

### Build locally

To use the latest code from this repository instead, build the library and publish it to your local Maven repository:

```shell
just publish-local
```

This runs `./kotlin publish mavenLocal`, which publishes the library to your local Maven repository (typically at `~/.m2/repository/`). You can then use it in any project that has your local Maven repository (`mavenLocal()`) configured as a dependency source:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}
```

```kotlin
// build.gradle.kts
implementation("org.kotlinbitcointools:bip21:0.2.0-SNAPSHOT")
```

## Documentation

You can [find the docs for this library here](https://kotlin-bitcoin-tools.github.io/bip21/index.html).

[BIP-0021]: https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki
[issues]: https://github.com/kotlin-bitcoin-tools/bip21/issues
[Maven Central]: https://central.sonatype.com/artifact/org.kotlinbitcointools/bip21
