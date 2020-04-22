# Hedvig app for Android

## Develop

1. Acquire Adyen credentials (you can find them in 1Password), place in the following paths:
    - app/src/${debug|staging|release}/res/values/adyen.xml
2. Download the schema (required to consume any changes in schema as well):
```bash
$ ./gradlew app:downloadApolloSchema
```
3. Build and install via Android Studio
