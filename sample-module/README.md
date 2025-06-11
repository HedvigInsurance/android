## Sample module

Copy me to create a new module.

Example steps for a module called "sample".

1. Rename the root directory to `sample`
2. Go to [com.hedvig.android](src/main/kotlin/com/hedvig/android) and make a subpackage of `sample`.
3. Add this new module as a `// implementation(projects.sample)` to whichever modules want to depend on this.
4. If it's not an android module, delete the AndroidManifest.xml file and change `id("hedvig.android.library")` to `id("hedvig.jvm.library")`.
5. Btw if it uses apollo, it should be an android module, with `id("hedvig.android.library")`.
6. Edit (or delete) this README of that new module to what's appropriate for it.
7. Run `./gradlew lint` to create new baseline file. It will fail first time, run it twice.
8. Profit $$$

P.S. Remember to use the `internal` visibility modifier by default instead of public unless you specifically *know* it needs to be public 🙈
