---
name: Release
about: Checklist for releasing a new version of the library
title: "Release <version>"
labels: release
assignees: ""
---

## Release <version>

- [ ] Fill out `CHANGELOG.md` for the new version.
- [ ] Make sure all CI checks pass on `master` (the `Test` and `Format Check` workflows).
- [ ] Bump the version in `module.yaml`, and commit using the `Bump version to <version>` message.
- [ ] Tag that commit locally, e.g. `v0.2.0`.
- [ ] Publish the library:
  - Set `mavenCentral.enabled: true` and `signArtifacts: true` in `module.yaml`, without committing that change.
  - Run `just publish-local` and inspect the local artifact to make sure it's good.
  - Run `./kotlin publish mavenCentral`, then release the deployment in the Central Portal.
  - Check that the new version shows up on [Maven Central](https://central.sonatype.com/artifact/org.kotlinbitcointools/bip21).
  - Revert the two settings in `module.yaml`.
- [ ] Push the commit and the tag to GitHub.
- [ ] Create a GitHub release from the tag, using the changelog entry as release notes.
- [ ] Bump the version to a new snapshot, e.g. `0.3.0-SNAPSHOT`, and commit using the `Bump version to <version>` message.
- [ ] Check that the documentation website was deployed with the new version.
- [ ] Open https://javadoc.io/doc/org.kotlinbitcointools/bip21/<version> so javadoc.io picks up the API docs for the new version.
