# Event Sourcing Kotlin Toolkit

This project aims to provide a toolkit to work with event sourcing, as well as some related architectural patterns such as CQRS and DDD, written 100% in Kotlin.

We aim to support all Kotlin targets relevant for server-side development, such as Kotlin/JVM, Kotlin/Native (Linux and macOS, arm64 and amd64) and Kotlin/JS.

A more detailed documentation and possibly a long-term roadmap coming soon :)

## Development

If you need to make changes, you can edit code however you like and test changes with:
```shell
./gradlew test
```

If you need to publish locally to test on other projects, you can use the following:

```shell
./gradlew -Pversion=local-SNAPSHOT publishToMavenLocal
```

If you're interested only in the JVM artefacts, you can run:

```shell
./gradlew -Pversion=local-SNAPSHOT publishJvmPublicationToMavenLocal
```

You can then change your dependency version to `local-SNAPSHOT` and make sure you have
the local maven repository enable if you're using Gradle, and your external project
should see your local changes.
