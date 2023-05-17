## Sample module

Copy me to create a new module.

Example steps for a module called "sample".

1. Rename the source folder to `sample`
2. Go to [com.hedvig.android](src/main/kotlin/com/hedvig/android) and make a subpackage of `sample`.
3. Go to [gradle settings](./../settings.gradle.kts) and add `include(":sample")` and sort lines appropriately.
4. Add this new module as a `implementation(projects.app.sample)` to whichever modules want to depend on this.
5. If it's an android module, edit the namespace inside the `android{}` block from `namespace = "com.hedvig.android.todo"` to `namespace = "com.hedvig.android.sample"`.
6. If it's not an android module, delete the AndroidManifest.xml file and change `id("hedvig.android.library")` to `id("hedvig.kotlin.library")`.
7. Edit (or delete) this README of that new module to what's appropriate for it.
8. Profit $$$

P.S. Remember to use the `internal` visibility modifier by default instead of public unless you specifically *know* it needs to be public 🙈
