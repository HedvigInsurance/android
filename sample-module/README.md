## Sample module

Copy me to create a new module.

Example steps for a module called "core-hedvig".

1. Rename the source folder to `core-hedvig`
2. Go inside [AndroidManifest](src/main/AndroidManifest.xml) and rename `package="com.hedvig.android.todo"` to `package="com.hedvig.android.core.hedvig"`
3. Go to [com.hedvig.android](src/main/java/com/hedvig/android) and rename package `todo` into `core` and make a subpackage of `hedvig`
4. Go to [gradle settings](./../settings.gradle.kts) and add `include(":core-hedvig")` and sort lines appropriately.
5. Add this new module as a `implementation(projects.coreHedvig)` to whichever modules want to depend on this
6. Edit (or delete) this README of that new module to what's appropriate for it.
7. Profit $$$

P.S. Remember to use the `internal` visibility modifier by default instead of public unless you specifically *know* it needs to be public 🙈
