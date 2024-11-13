## lokalise-gradle-plugin

A plugin to provide the `./gradlew downloadStrings` task which downloads all strings
from lokalise into the path specified in `outputDirectory`

An example of usage is:

```kotlin
plugins {
  id("hedvig.gradle.plugin")
  // Other plugins
  id("hedvig.android.lokalise")
}

lokalise {
  lokaliseProjectId.set("...")
  lokaliseToken.set("...")
  outputDirectory.set(file("src/main/res"))
  // Optionally
  downloadConfig.set(com.hedvig.android.lokalise.config.DownloadConfig())
}

```

If you're editing this from Android Studio, chances are a lot of things will be unresolved. Use
IntelliJ instead for a proper editing experience.