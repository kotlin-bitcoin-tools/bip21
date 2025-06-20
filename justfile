default:
  just --list

test:
  ./gradlew test

onetest TEST:
  ./gradlew test --tests {{TEST}}

serve:
  ./gradlew dokkaGeneratePublicationHtml && rm -rf ./docs/api/ && mv ./build/dokka/html ./docs/api && mkdocs serve

dokka:
  ./gradlew dokkaGeneratePublicationHtml

publishlocal:
  ./gradlew publishToMavenLocal --rerun-tasks
