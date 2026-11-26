---
name: render-previews
description: Use when a `@Preview` in this repo needs to be seen without a device, emulator or Android Studio: to prove what a UI change looks like before calling it done, to check every state, device and theme a preview declares, to read a rendered hierarchy as text, or to find previews that no longer render. Works for every module on the `:app` classpath, Android-only `src/main` modules included. Not for comparing throwaway layout candidates in a KMP module; that is render-composables-on-jvm.
---

# Rendering previews

Google's Android CLI builds `:app` debug through AGP's `generateDebugComposePreviewRunfiles` task
and renders any `@Preview` on that classpath through Layoutlib, the renderer behind Studio's
preview panel, so text, fonts, Lokalise strings and `HedvigPreview`'s fake image loader come out
as Studio shows them. Private previews, `HedvigPreview`-style MultiPreviews and `PreviewParameter`
providers all expand. The first build of a checkout takes a few minutes, so run it in the
background; the usual repo prerequisites (strings, schema, see Essential Setup in `CLAUDE.md`)
apply. With the daemon warm, a render takes seconds.

Needs the CLI: `brew tap android/tap && brew install android-cli`, then `android -V` prints a
version like `1.1.x`. If it prints `The "android" command is deprecated` instead, the dead legacy
script in `~/Library/Android/sdk/tools/` is shadowing it on `PATH` (common in non-login shells);
call `$(brew --prefix)/bin/android` by full path in that case.

## Decide first: which renderer

This renders the code as it is. It cannot compare candidates without an edit between renders.

- **Verifying a change**, in any module: this skill. Render the previews of the file you touched
  and look at every variant before calling the change done.
- **Choosing between candidates** in a KMP module with a JVM target: render-composables-on-jvm,
  which composes throwaway variants side by side without touching committed code. Its text
  shaping is Skia's, so confirm the winner through its preview once implemented.
- **Choosing between candidates** in an Android-only module (`hedvig.android.library`): this
  skill, one edit-and-render cycle per candidate against the existing preview. A warm cycle is
  seconds.
- **A device** only for runtime behaviour: animation, ripples, scrolling, anything after the first frame.

A composable you need to show and that has no preview gets one, next to its siblings: annotate
with `@HedvigPreview` from `design-system-hedvig` and wrap in `HedvigTheme { Surface(color =
HedvigTheme.colorScheme.backgroundPrimary) { ... } }`. A preview that shows the finished state
stays in the change.

## Workflow

Run from the checkout root. Output goes outside the repo; nothing here needs cleaning up.

1. Find the previews for the file you touched. The FQN shape is `<package>.<FileName>Kt.<previewFunction>`,
   and the list is every variant of every preview in the app, so filter it:
   ```bash
   OUT=$(mktemp -d)
   android --no-metrics preview list :app --all --format=json \
     | grep -o '"methodFqn": *"[^"]*OnboardingWelcomeDestinationKt[^"]*"' | sort -u
   ```
2. Render them, repeating `--fqn=` once per method (never joined into one string, or the CLI
   reports the whole string as a missing class):
   ```bash
   android --no-metrics preview render :app --daemon --component-tree --format=json -o "$OUT" \
     --fqn=com.hedvig.android.feature.onboarding.ui.welcome.OnboardingWelcomeDestinationKt.PreviewOnboardingWelcomeScreen \
     --fqn=com.hedvig.android.design.system.hedvig.ButtonKt.PreviewRoundedButtons
   ```
   `--daemon` keeps the renderer alive for ten minutes after the last render; `--no-image`
   writes only the tree.
3. Read the result. Stdout may start with a progress line before the JSON, so parse from the first
   `{`. Each entry in `results[]` has `methodFqn`, `imagePath`, `componentTreePath` and, when that
   variant failed, `error` with `problems[].stackTrace`. **`error` is the failure signal:** a broken
   variant still gets an `imagePath` (a 1x1 PNG), and the command exits 1 with `success: false` if
   any variant has one.
4. Look at the PNGs with the Read tool. Files land at `$OUT/<package path>/<FileName>Kt/<preview>_<n>.png`
   with `_<n>` ordered device-config-major, `PreviewParameter`-value-minor, so `_0` is the first
   provider value in the first config; confirm from each JSON's `uiMode` and `deviceId` rather
   than from the number. Use the companion JSON for anything structural: it has `width`, `height`,
   `densityDpi`, `fontScale`, `uiMode`, `deviceId` and a `components` tree where each node has
   `name`, `bounds`, `sourceLocation` (file and line), `modifiers`, `parameters` (text values
   included) and `semantics`. "The loading state has a `ThreeDotsLoading` node" or "the button has
   a content description" is a text check, not a pixel one.
5. A variant with an `error` is a bug in the preview or the screen, unless the stack trace names
   something Layoutlib cannot host (`AndroidView` content such as players, Rive). Fix it in the
   same change when it is yours; otherwise report the FQN and the exception.

## Things that will trip you up

- The CLI runs Gradle on its own JDK. A parallel `./gradlew` in the same checkout can sit waiting
  on its lock; run compile checks after the render, not alongside it.
- It renders a static first frame: `AnimatedVisibility` is at its initial state and nothing scrolls.
- Images come from `HedvigPreview`'s fake Coil loader, so network images are placeholders by design.
- It is not screenshot testing. There is no golden image and no diffing; you compare by looking,
  or by asserting on the tree.
