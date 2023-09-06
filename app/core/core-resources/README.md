## core-resources

Module to contain the strings generated from `./gradlew downloadStrings`

Exists since we're automatically getting the strings from lokalise, and they need to be shared across all modules 
which may want to access them. This should depend on no other modules and have all modules which need access to string
resources depend on it.
