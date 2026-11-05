[group("Core")]
[doc("List all available commands.")]
@list:
  just --list --unsorted

[group("Core")]
[doc("Open repository on GitHub.")]
repo:
  open https://github.com/kotlin-bitcoin-tools/bip21

[group("Tests")]
[doc("Run all unit tests.")]
test:
  ./kotlin test

[group("Format")]
[doc("Format all Kotlin source files with ktfmt.")]
format: check-ktfmt-version
  ktfmt --kotlinlang-style --enable-editorconfig src/ test/

[group("Format")]
[doc("Check that all Kotlin source files are formatted, without changing them.")]
format-check: check-ktfmt-version
  #!/usr/bin/env bash
  set -euo pipefail
  if ! ktfmt --kotlinlang-style --enable-editorconfig --dry-run --set-exit-if-changed src/ test/; then
    echo "Error: the files above need formatting. Run 'just format' to fix them." >&2
    exit 1
  fi

# The version of ktfmt must be 0.65
[private]
check-ktfmt-version:
  #!/usr/bin/env bash
  set -euo pipefail
  version=$(ktfmt --version | grep -oE '[0-9]+\.[0-9]+')
  if [ "$version" != "0.65" ]; then
    echo "Error: ktfmt 0.65 is required, but found $version" >&2
    exit 1
  fi

[group("Docs")]
[doc("Build the documentation website.")]
docs-build:
  cd docs && uv run zensical build --clean

[group("Docs")]
[doc("Serve the documentation website locally.")]
docs-serve:
  cd docs && uv run zensical serve

[group("Docs")]
[doc("Remove the documentation website build output and Python environment.")]
docs-clean:
  rm -rf docs/.venv/ docs/.cache/ docs/site/

[group("Publish")]
[doc("Publish the library to Maven Local.")]
publish-local:
  ./kotlin publish mavenLocal
