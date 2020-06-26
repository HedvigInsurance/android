# Hedvig app for Android

## Develop

1. Acquire Adyen credentials (you can find them in 1Password), place in the following paths:
    - `app/src/${debug|staging|release}/res/values/adyen.xml`
2. Acquire Lokalise credentials (you can find them in 1Password), place in the following file:
    - `lokalise.properties`
3. Download the schema (required to consume any changes in schema as well):
```bash
$ ./gradlew apollo:downloadApolloSchema
```
4. Download lokalise translations (required to consume latest translations as well):
```bash
$ ./gradlew app:downloadStrings
```
5. Build and install via Android Studio
