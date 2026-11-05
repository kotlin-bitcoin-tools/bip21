# Install
The library is available on [Maven Central](https://central.sonatype.com/artifact/org.kotlinbitcointools/bip21) under the group ID `org.kotlinbitcointools` and the artifact ID `bip21`:

```kotlin title="build.gradle.kts"
implementation("org.kotlinbitcointools:bip21:0.1.0")
```

## Build locally
To use the latest code from the repository instead, build the library and publish it to your local Maven repository:

```shell
just publish-local
```

This runs `./kotlin publish mavenLocal`, which publishes the library to your local Maven repository (typically at `~/.m2/repository/` for macOS and Linux systems). You can then use it in any project that has your local Maven repository (`mavenLocal()`) configured as a dependency source:

```kotlin title="settings.gradle.kts"
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}
```

```kotlin title="build.gradle.kts"
implementation("org.kotlinbitcointools:bip21:0.2.0-SNAPSHOT")
```
