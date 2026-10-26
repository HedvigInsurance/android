---
name: render-composables-on-jvm
description: Use when you need to see what a composable looks like, or compare several layout, alignment, wrapping or styling variants of one as images, in a KMP module with a JVM target, without an emulator, a device, Android Studio or an app build. Also use when a Compose preview would do but you need the pictures from a script.
---

# Rendering composables on the JVM

Any `hedvig.multiplatform.library*` module renders on the desktop through Skia, so a `jvmTest`
can compose real design-system components inside `HedvigTheme`, with the Hedvig fonts and
Lokalise strings, and write the result to a PNG. No device, no emulator, no `:app` build.
First run compiles the module's JVM dependency chain (a few minutes with the build cache),
later runs are incremental and take seconds.

The test is a tool, not a test. It lives in the module's own `jvmTest` source set, which can
reach the module's `internal` composables, and it is deleted before committing.

## Workflow

1. Check the module has a JVM target and the desktop test deps. Look for this in its
   `build.gradle.kts`; add it if missing:
   ```kotlin
   jvmTest.dependencies {
     implementation(libs.jetbrains.compose.ui.test)
     implementation(org.jetbrains.compose.ComposePlugin.DesktopDependencies.currentOs)
   }
   ```
2. Copy `RenderVariantsTemplate.kt` from this directory into
   `<module>/src/jvmTest/kotlin/<package>/`, fix the package line, and fill the `variants`
   list: one label plus one composable per thing to compare. Put every variant of one input
   in the same image, so they share the frame.
3. Run it. The Gradle path is the module's directory name, so `app/feature/feature-claim-chat`
   is `:feature-claim-chat`:
   ```bash
   JAVA_HOME=$(/usr/libexec/java_home) ./gradlew :<module>:jvmTest --tests '*RenderVariants*'
   ```
4. Images land in `<module>/build/render/`. Copy them out, stitch several with Pillow if a
   side-by-side helps, and look at them before sending them on.
5. Delete the test file. It is never committed.

## What the desktop renderer gets wrong

Skia is not Android's text stack. Everything layout-related matches, these do not:

- `LineBreak.Heading` and other balanced or high-quality break strategies are ignored; lines
  break greedily. Simulate with explicit `\n` and say so, then confirm on a device.
- Hyphenation, `includeFontPadding` and emoji fonts differ.
- Ripples, `AndroidView` content and anything from `androidMain` are unavailable.

## Things that will trip you up

- **The surface is a fixed canvas.** `runDesktopComposeUiTest(width, height)` sizes it in
  pixels; content past the bottom is silently clipped. Make it taller than you think.
- **Density is 1 unless you set it.** `LocalDensity provides Density(2.5f, 1f)` gives phone
  pixel sizes; 375dp becomes 940px.
- **Explicit `\n` changes measurement.** Text with a newline measures to its widest line, so a
  simulated balanced variant hugs on its own. Real wrapped text measures to the full width.
- **`captureToImage()` is the whole surface.** Draw a themed background behind the content or
  the image comes out black.
- **Render the stateless layer.** `Res.string` and `Res.drawable` resolve on the JVM, but a
  screen that takes a ViewModel does not; call the composable that takes plain state.
- **Gradle warnings about `System::load`** from skiko and native-platform are noise.
