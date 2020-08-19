# Hedvig app for Android

## Develop

1. Acquire Adyen credentials (you can find them in 1Password), place in the following paths:
    - `app/src/${debug|staging|release}/res/values/adyen.xml`
2. Acquire Lokalise credentials (you can find them in 1Password), place in the following file:
    - `lokalise.properties`
3. Acquire Mixpanel credentials (you can find them in 1Password), place in the following paths:
    - `app/src/${debug|staging|release}/res/values/mixpanel.xml`
3. Download the schema (required to consume any changes in schema as well):
```bash
$ ./gradlew apollo:downloadApolloSchema
```
4. Download lokalise translations (required to consume latest translations as well):
```bash
$ ./gradlew app:downloadStrings
```
5. Build and install via Android Studio

## Build Types

* Release: `com.hedvig.app` Build for the customer on Play Store. Using production backend
* Staging: `com.hedvig.test.app` Build for internal testing using Firebase App Distribution. Using staging backend
* Develop: `com.hedvig.dev.app` Build for development. Using staging backend