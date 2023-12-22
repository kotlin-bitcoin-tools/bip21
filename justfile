jvmtest:
  ./gradlew jvmTest

onetest TEST:
  ./gradlew test --tests {{TEST}}

serve:
  mkdocs serve
