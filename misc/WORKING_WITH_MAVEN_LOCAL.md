## Working with mavenLocal()

For odyssey, or any future internal library, in order to iterate fast when doing changes to it, one 
can and should avoid doing the entire loop of publishing to a remote artifact registry like GitHub
packages or whatnot. Instead, we can make use of mavenLocal() in order to speed up development to 
as close levels as possible to developing on the same repo. Here are the steps to do so:

### Setup

#### In the library repository
1. Update the library version to something and remember it.
2. Run `./gradlew publishToMavenLocal` (`./gradlew common:publishToMavenLocal` for odyssey)

#### In this repository
1. Go to the [settings.gradle.kts](/settings.gradle.kts) file and in the 
 `dependencyResolutionManagement` block, and add `mavenLocal()` *before* the repository which 
 would normally resolve the library you're trying to test. [1]
2. Go to [libs.versions.toml](/gradle/libs.versions.toml) and update the version to whatever 
 version of the library you're building 
3. Go to the place where we declare the dependency to that library e.g. 
 ```implementation(libs.hedvig.odyssey)``` 
 and change it to
 ```
 implementation(libs.hedvig.odyssey) {
   isChanging = true
 }
 ```
 This will mean that gradle will not try to use the cache to fetch the library, but will always 
 look in the `repositories {}` block instead and fetch the latest version which should be what you 
 just built.

### To iterate

1. In the library: Do the code changes you need.
2. In the library: Run `./gradlew publishToMavenLocal` (`./gradlew common:publishToMavenLocal` for odyssey) 
3. In this repository: Sync gradle
4. In this repository: (optional) fix the code if library changed the API
5. In this repository: Run the app

### After finishing

Remember not to commit any of this to production.
1. Remove the `isChanging` configuration
2. Remove the `mavenLocal()` declaration completely
3. Possibly undo the version bump if you don't want to commit to the next version yet

---
 
[1] Practically, this means that for Odyssey, it needs to be above the block which targets the GitHub
packages maven URL and so on.
This works because for each dependency it looks at the declared repositories one by one and as
soon as it resolves anywhere it stops looking for anything below it, effectively resolving the
library from mavenLocal() and ignoring the real dependency from remotely.