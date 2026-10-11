# KMP-readiness migration implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the module dependency graph readable and useful as a migration yardstick, flip the convention plugins so KMP modules no longer get iOS targets by default, then convert every module that has no Android ties to KMP, in dependency order.

**Architecture:** Three independent deliverables in sequence. The Groovy graph script is rewritten in place to filter, classify and transitively reduce the module graph into one SVG plus a Markdown summary. The convention plugins in `build-logic` are changed so `hedvig.multiplatform.library` means JVM plus Android and a new `.ios` id opts a module into the umbrella closure. Module conversions are then mechanical: plugin swap, source-set move, dependency block move, graph regeneration.

**Tech Stack:** Gradle 9 with Kotlin DSL convention plugins, Kotlin 2.4.20, AGP 9.4.1 (`com.android.kotlin.multiplatform.library`), Compose Multiplatform 1.12.1, Metro DI, KSP, graphviz (`tred`, `dot`).

**Spec:** `docs/superpowers/specs/2026-10-12-kmp-readiness-migration-design.md`

## Global Constraints

- Everything stays local: no `git push`, no PR creation, no umbrella release. Every task ends in a local commit on the named branch.
- Work happens in the `android_copy` clone (`/Users/stylianosgakis/hedvig/apps/android_copy`), branched off `develop`. Run every command from that directory.
- No em-dashes anywhere in commit messages, comments or docs. Use commas, periods or colons.
- Comments describe current code only. No "replaces", "used to", "for now", "instead of".
- Every conversion PR regenerates the graph and commits `misc/images/modularization-graph.svg` and `misc/images/modularization-summary.md` with the change.
- A module that needs more than the change listed in its task is dropped from the wave, noted in the commit message, and the task continues with the rest.
- `./gradlew ktlintFormat` before every commit that touches Kotlin.
- The graph task is run as `./gradlew :generateProjectDependencyGraph --no-configuration-cache`.
- Target names for KMP modules: Android compilation is `compileAndroidMain`, JVM compilation is `compileKotlinJvm`, tests are `jvmTest`. There is no debug variant on library modules.

## Review Focus

1. **Graphviz missing from PATH**: the task must fail with a message naming `brew install graphviz`, not a cryptic `IOException`. Pinned in Task 1 step 5.
2. **`tred` dropping node attributes**: if the reduced dot lost `fillcolor` or `penwidth`, the SVG would be grey and the iOS border gone. Pinned in Task 1 step 4 (count of `fillcolor` lines after reduction equals the node count).
3. **A closure module left on the plain plugin id**: `:umbrella` must fail to build, not silently drop the module. Pinned in Task 5 step 6 (XCFramework build) and Task 5 step 4 (count of `.ios` ids equals 47).
4. **A converted module whose JVM tests relied on Android stubs** (`testOptions.unitTests.isReturnDefaultValues`): on the JVM target these tests either pass or fail loudly. Pinned by the `jvmTest` step in every conversion task; a failure means the module is dropped from the wave.
5. **Apollo operations left in `src/main/graphql`**: Apollo would generate nothing and the compile fails on missing classes. Pinned by the `compileAndroidMain` / `compileKotlinJvm` step in every conversion task that has `apollo(...)`.

---

## Phase A: graph detour

Branch: `kmp/graph-detour` off `develop`.

```bash
cd /Users/stylianosgakis/hedvig/apps/android_copy
git checkout develop && git pull --ff-only
git checkout -b kmp/graph-detour
```

### Task 1: Rewrite the graph script

**Files:**
- Modify: `gradle/projectDependencyGraph.gradle` (replace the whole file)

**Interfaces:**
- Produces: `build/reports/dependency-graph/project.dot` (raw), `project-reduced.dot` (after `tred`), `project-final.dot` (with legend), and `misc/images/modularization-graph.svg`.
- Produces the Groovy closures `kindOf(Project)`, `mainDependencies(Project)`, `iosClosure` and `included` that Task 2 extends.

- [ ] **Step 1: Replace the script**

Write this as the full content of `gradle/projectDependencyGraph.gradle`:

```groovy
// Module dependency graph shown in README.md under "Module structure".
// Run: ./gradlew :generateProjectDependencyGraph --no-configuration-cache
// Needs graphviz on the PATH for `tred` and `dot`: brew install graphviz

import org.gradle.api.artifacts.ProjectDependency

def font = "Helvetica"

// Projects left out of the picture, each with its reason.
def excludedProjects = [
  ':app',                // depends on every feature, so its edges carry no information
  ':hedvig-lint',        // attached to every module by hedvig.gradle.plugin (lintChecks)
  ':hedvig-ktlint',      // attached to every module by hedvig.gradle.plugin (ktlint)
  ':logging-public',     // attached to every module by hedvig.gradle.plugin
  ':tracking-core',      // attached to every module by hedvig.gradle.plugin
  ':test-clock',         // test utility
  ':accessibility-test', // test module
]

// Module kind by convention plugin. First match wins. The colour is the node background.
def kinds = [
  [id: 'hedvig.multiplatform.library',     label: 'KMP, iOS targets', color: '#ffd2b3'],
  [id: 'hedvig.multiplatform.library.jvm', label: 'KMP, no iOS',      color: '#fff3b3'],
  [id: 'hedvig.android.library',           label: 'Android library',  color: '#baffc9'],
  [id: 'hedvig.jvm.library',               label: 'JVM library',      color: '#ffb3ba'],
  [id: 'hedvig.android.application',       label: 'Application',      color: '#cfe0ff'],
]
def otherKind = [id: '', label: 'Other', color: '#eeeeee']

task generateProjectDependencyGraph {
  notCompatibleWithConfigurationCache("Reads the project model inside doLast")
  doLast {
    ['tred', 'dot'].each { tool ->
      def check = ['which', tool].execute()
      check.waitFor()
      if (check.exitValue() != 0) {
        throw new GradleException("`$tool` not found on PATH. Install graphviz: brew install graphviz")
      }
    }

    def isTestProject = { Project p -> p.path.endsWith('-test') || p.path.endsWith('-fake') }
    def isIncluded = { Project p ->
      p != rootProject && !excludedProjects.contains(p.path) && !isTestProject(p)
    }
    // Test, lint and KSP processor configurations describe the build, not the product.
    def isMainConfiguration = { Configuration c ->
      def n = c.name.toLowerCase()
      !(n.contains('test') || n.contains('lint') || n.contains('ktlint') || n.startsWith('ksp'))
    }
    def kindOf = { Project p -> kinds.find { p.plugins.hasPlugin(it.id) } ?: otherKind }

    // Project dependencies declared on main configurations: target -> declared through an api configuration
    def mainDependencies = { Project p ->
      def result = new LinkedHashMap<Project, Boolean>()
      p.configurations.findAll(isMainConfiguration).each { config ->
        def isApi = config.name.toLowerCase().endsWith('api')
        config.dependencies.withType(ProjectDependency).each { dep ->
          def target = rootProject.findProject(dep.path)
          if (target == null || target == p) return
          result[target] = (result[target] ?: false) || isApi
        }
      }
      result
    }

    // Everything :umbrella reaches through main configurations is compiled into HedvigShared for iOS.
    def iosClosure = new LinkedHashSet<Project>()
    def closureQueue = [rootProject.project(':umbrella')]
    while (!closureQueue.isEmpty()) {
      def p = closureQueue.remove(0)
      if (!iosClosure.add(p)) continue
      closureQueue.addAll(mainDependencies(p).keySet())
    }

    def included = rootProject.allprojects.findAll(isIncluded).sort { it.path }

    def dotDir = rootProject.layout.buildDirectory.dir('reports/dependency-graph').get().asFile
    dotDir.mkdirs()
    def rawDot = new File(dotDir, 'project.dot')
    def reducedDot = new File(dotDir, 'project-reduced.dot')
    def finalDot = new File(dotDir, 'project-final.dot')

    rawDot.withWriter { w ->
      w << 'digraph {\n'
      w << "  graph [label=\"${rootProject.name} modules\\n \", labelloc=t, fontsize=30, fontname=\"$font\", rankdir=LR, ranksep=1.0, nodesep=0.15];\n"
      w << "  node [style=filled, fontname=\"$font\"];\n"
      w << "  edge [fontname=\"$font\"];\n\n"
      included.each { p ->
        def kind = kindOf(p)
        def traits = ["fillcolor=\"${kind.color}\""]
        if (iosClosure.contains(p)) traits << 'penwidth=3'
        w << "  \"${p.path}\" [${traits.join(', ')}];\n"
      }
      w << '\n'
      included.each { from ->
        mainDependencies(from).each { to, isApi ->
          if (!isIncluded(to)) return
          w << "  \"${from.path}\" -> \"${to.path}\"${isApi ? '' : ' [style=dotted]'}\n"
        }
      }
      w << '}\n'
    }

    def tred = ['tred', rawDot.absolutePath].execute()
    def tredOut = new StringBuilder()
    def tredErr = new StringBuilder()
    tred.waitForProcessOutput(tredOut, tredErr)
    if (tred.exitValue() != 0) throw new GradleException("tred failed: $tredErr")
    reducedDot.text = tredOut.toString()

    def legend = new StringBuilder()
    legend << '  subgraph cluster_legend {\n'
    legend << "    label=\"Legend\"; fontsize=16; fontname=\"$font\"; style=dashed;\n"
    legend << '    node [shape=box];\n'
    (kinds + [otherKind]).each { k ->
      legend << "    \"legend ${k.label}\" [label=\"${k.label}\", fillcolor=\"${k.color}\"];\n"
    }
    legend << '    "legend ios" [label="in the iOS umbrella closure", fillcolor="#ffffff", penwidth=3];\n'
    legend << '    "legend api a" [label="a"]; "legend api b" [label="b"];\n'
    legend << '    "legend api a" -> "legend api b" [label="api"];\n'
    legend << '    "legend impl a" [label="a"]; "legend impl b" [label="b"];\n'
    legend << '    "legend impl a" -> "legend impl b" [label="implementation", style=dotted];\n'
    legend << "    \"legend excluded\" [shape=plaintext, fillcolor=\"#ffffff\", label=\"Not shown: ${excludedProjects.join(', ')}, *-test, *-fake\"];\n"
    legend << '  }\n'
    def reducedText = reducedDot.text
    def closingBrace = reducedText.lastIndexOf('}')
    finalDot.text = reducedText.substring(0, closingBrace) + legend.toString() + '}\n'

    def svg = rootProject.file('misc/images/modularization-graph.svg')
    def dot = ['dot', '-Tsvg', '-o', svg.absolutePath, finalDot.absolutePath].execute()
    def dotOut = new StringBuilder()
    def dotErr = new StringBuilder()
    dot.waitForProcessOutput(dotOut, dotErr)
    if (dot.exitValue() != 0) throw new GradleException("dot failed: $dotErr")

    println("Module dependency graph written to ${svg}")
  }
}
```

- [ ] **Step 2: Run the task**

Run: `./gradlew :generateProjectDependencyGraph --no-configuration-cache -q`
Expected: ends with `Module dependency graph written to .../misc/images/modularization-graph.svg`, exit 0.

- [ ] **Step 3: Assert the node set**

```bash
D=build/reports/dependency-graph/project.dot
echo "app: $(grep -c '":app"' $D)  tests: $(grep -cE '":[^"]*-(test|fake)"' $D)  hubs: $(grep -cE '":(hedvig-lint|hedvig-ktlint|logging-public|tracking-core|test-clock|accessibility-test)"' $D)"
echo "nodes: $(grep -cE '^  ":[^"]+" \[' $D)  edges: $(grep -c ' -> ' $D)  ios-closure: $(grep -c 'penwidth=3' $D)"
```
Expected: `app: 0  tests: 0  hubs: 0`; nodes between 120 and 130; `ios-closure: 45` (47 modules in the closure, minus `:logging-public` and `:tracking-core` which are excluded nodes).

- [ ] **Step 4: Assert reduction kept attributes**

```bash
R=build/reports/dependency-graph/project-reduced.dot
F=build/reports/dependency-graph/project-final.dot
echo "raw edges: $(grep -c ' -> ' build/reports/dependency-graph/project.dot)  reduced edges: $(grep -c ' -> ' $R)"
echo "fillcolor lines reduced: $(grep -c 'fillcolor' $R)  nodes raw: $(grep -cE '^  ":[^"]+" \[' build/reports/dependency-graph/project.dot)"
grep -c 'cluster_legend' $F
ls -la misc/images/modularization-graph.svg
```
Expected: reduced edges roughly 200 to 260 and well under raw; `fillcolor` count equals the raw node count; `cluster_legend` count 1; SVG under 1 MB.

- [ ] **Step 5: Assert the graphviz guard**

Run: `PATH=/usr/bin:/bin ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q 2>&1 | grep -c 'brew install graphviz'`
Expected: `1` (the task fails with the install hint). If the sandbox cannot run Gradle with that PATH, run it through `env PATH=/usr/bin:/bin:$(dirname $(which java)) ./gradlew ...` so that Java resolves but graphviz does not.

- [ ] **Step 6: Open the SVG and look**

Run: `open misc/images/modularization-graph.svg`
Expected: left-to-right layout, legend cluster visible, five node colours, bold borders on the closure, every label readable without zoom at 100%.

- [ ] **Step 7: Commit**

```bash
git add gradle/projectDependencyGraph.gradle misc/images/modularization-graph.svg
git commit -m "build: make the module graph readable

Transitive reduction, test modules and the convention-plugin hubs left out,
nodes coloured by convention plugin, bold border on the iOS umbrella closure,
SVG output instead of a PNG that hit the 32767 px width ceiling."
```

### Task 2: Summary file

**Files:**
- Modify: `gradle/projectDependencyGraph.gradle` (append inside `doLast`, after the `println`)

**Interfaces:**
- Consumes: `kinds`, `otherKind`, `kindOf`, `mainDependencies`, `iosClosure`, `included`, `isIncluded` from Task 1.
- Produces: `misc/images/modularization-summary.md`.

- [ ] **Step 1: Append the summary writer**

Add this immediately before the closing `}` of `doLast`:

```groovy
    def kmpIds = kinds.findAll { it.label.startsWith('KMP') }*.id
    def isKmp = { Project p -> kmpIds.contains(kindOf(p).id) }
    def dependents = new LinkedHashMap<Project, List<Project>>()
    included.each { from ->
      mainDependencies(from).keySet().findAll(isIncluded).each { to ->
        dependents.computeIfAbsent(to) { [] } << from
      }
    }
    def blockers = included
      .findAll { !isKmp(it) && kindOf(it).id != 'hedvig.android.application' && !(dependents[it] ?: []).isEmpty() }
      .sort { a, b -> (dependents[b].size() <=> dependents[a].size()) ?: (a.path <=> b.path) }

    def summary = rootProject.file('misc/images/modularization-summary.md')
    summary.withWriter { w ->
      w << '# Module summary\n\n'
      w << 'Written by `./gradlew :generateProjectDependencyGraph`. Counts cover the modules the graph shows.\n\n'
      w << '| Kind | Modules |\n|---|---|\n'
      (kinds + [otherKind]).each { k ->
        w << "| ${k.label} | ${included.count { kindOf(it).is(k) }} |\n"
      }
      w << "| In the iOS umbrella closure (all modules) | ${iosClosure.size()} |\n\n"
      w << '## Non-KMP modules and what depends on them\n\n'
      w << 'Converting a module near the top unblocks the most dependents.\n\n'
      blockers.each { b ->
        def names = dependents[b].sort { it.path }.collect { "`${it.path}`" }.join(', ')
        w << "- `${b.path}` (${kindOf(b).label}) <- ${names}\n"
      }
    }
    println(summary.text)
```

- [ ] **Step 2: Run and inspect**

Run: `./gradlew :generateProjectDependencyGraph --no-configuration-cache -q`
Expected: the summary prints. The kind table shows `KMP, iOS targets` 63 (the 63 under `app/` plus `sample-module-kmp` and `design-showcase-desktop`, minus the two excluded hubs), `KMP, no iOS` 1, `Android library` 46 (47 minus `:accessibility-test`), `JVM library` 22 (35 minus eleven `-test` modules, `auth-event-fake` and `:test-clock`), closure 47. Expect `:core-demo-mode`, `:data-addons`, `:auth-core-public` and `:compose-pager-indicator` near the top of the blockers list; the two KSP processor modules must not appear in it.

- [ ] **Step 3: Assert no volatile content**

Run: `grep -cE '20[0-9]{2}-[0-9]{2}-[0-9]{2}|[0-9]{2}:[0-9]{2}' misc/images/modularization-summary.md`
Expected: `0` (no dates or times, so the file only changes when the graph does).

- [ ] **Step 4: Commit**

```bash
git add gradle/projectDependencyGraph.gradle misc/images/modularization-summary.md
git commit -m "build: write a module summary next to the graph

Count per module kind, the size of the iOS umbrella closure, and every
non-KMP module with the modules that depend on it."
```

### Task 3: README, PNG removal, baseline

**Files:**
- Modify: `README.md:30-35`
- Delete: `misc/images/modularization-graph.png`

- [ ] **Step 1: Update the README section**

Replace the `## Module structure` section body with:

```markdown
## Module structure

![Module graph](misc/images/modularization-graph.svg "Module dependency graph")

Generated from `./gradlew :generateProjectDependencyGraph --no-configuration-cache`, which also writes
[`misc/images/modularization-summary.md`](misc/images/modularization-summary.md) with a count per module
kind and the non-KMP modules that other modules still depend on. Requires graphviz: `brew install graphviz`.
Test modules, `:app`, and the modules the convention plugin attaches to everything are left out.
```

- [ ] **Step 2: Delete the PNG and update the CLAUDE.md command**

Run: `git rm misc/images/modularization-graph.png`

In `CLAUDE.md`, under "Common Development Commands", change the graph line to:

```bash
# Generate module dependency graph and summary (requires graphviz: brew install graphviz)
./gradlew :generateProjectDependencyGraph --no-configuration-cache
```

- [ ] **Step 3: Confirm nothing else references the PNG**

Run: `grep -rn "modularization-graph.png" --include='*.md' --include='*.yml' --include='*.kts' --include='*.gradle' . | grep -v /build/`
Expected: no output.

- [ ] **Step 4: Regenerate once more and commit the baseline**

```bash
./gradlew :generateProjectDependencyGraph --no-configuration-cache -q
git add README.md CLAUDE.md misc/images/modularization-graph.svg misc/images/modularization-summary.md
git commit -m "docs: reference the SVG module graph and summary from the README"
```

Expected: `git status` clean. This commit's SVG and summary are the migration baseline.

---

## Phase B: convention plugin changes

Branch: `kmp/plugin-ios-opt-in` off `develop` (not off the graph branch; the graph script's kind table is edited here in a way that is correct once both land, see Task 5 step 3).

```bash
git checkout develop
git checkout -b kmp/plugin-ios-opt-in
```

If Phase A has not merged yet, cherry-pick its three commits onto this branch first so the graph can be regenerated here: `git cherry-pick <three shas from kmp/graph-detour>`.

### Task 4: Plugin classes and registration

**Files:**
- Modify: `build-logic/convention/src/main/kotlin/KotlinMultiplatformLibraryConventionPlugin.kt:1-30`
- Modify: `build-logic/convention/build.gradle.kts` (the `gradlePlugin { plugins { } }` block)

**Interfaces:**
- Produces plugin id `hedvig.multiplatform.library` (JVM, plus Android through `.android`) and `hedvig.multiplatform.library.ios` (adds `iosArm64`, `iosSimulatorArm64`). Removes `hedvig.multiplatform.library.jvm`.

- [ ] **Step 1: Replace the two plugin classes**

In `KotlinMultiplatformLibraryConventionPlugin.kt`, replace everything from the first `class` through the end of `KotlinMultiplatformJvmLibraryConventionPlugin` with:

```kotlin
/**
 * A Kotlin Multiplatform library with a JVM target, plus an Android target when
 * `hedvig.multiplatform.library.android` is also applied. Never part of the iOS umbrella.
 */
class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.applyKotlinMultiplatform(withIos = false)
  }
}

/**
 * A Kotlin Multiplatform library that `:umbrella` exports to iOS, directly or transitively. Adds the
 * iOS targets on top of the JVM and Android ones, so every `expect` in the module needs a native actual.
 */
class KotlinMultiplatformIosLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.applyKotlinMultiplatform(withIos = true)
  }
}
```

Leave `applyKotlinMultiplatform` and `configureKotlinMultiplatform` as they are.

- [ ] **Step 2: Register the ids**

In `build-logic/convention/build.gradle.kts`, replace

```kotlin
    createPlugin("hedvig.multiplatform.library.jvm", "KotlinMultiplatformJvmLibraryConventionPlugin")
```

with

```kotlin
    createPlugin("hedvig.multiplatform.library.ios", "KotlinMultiplatformIosLibraryConventionPlugin")
```

- [ ] **Step 3: Compile build-logic**

Run: `./gradlew -p build-logic :convention:build -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add build-logic
git commit -m "build: make iOS targets an explicit opt-in for KMP modules

hedvig.multiplatform.library now builds JVM and Android only. A module that
:umbrella exports applies hedvig.multiplatform.library.ios."
```

### Task 5: Move the closure to the `.ios` id

**Files:**
- Modify: the 47 build files listed in step 1
- Modify: `app/feature/feature-claim-chat/build.gradle.kts:2`
- Modify: `gradle/projectDependencyGraph.gradle` (kinds table)
- Modify: `CLAUDE.md` (Convention Plugins list, around the `hedvig.multiplatform.library` bullets)

- [ ] **Step 1: Rename the id in every closure module**

```bash
for f in \
  app/apollo/apollo-core/build.gradle.kts \
  app/apollo/apollo-octopus-public/build.gradle.kts \
  app/apollo/apollo-operation-error/build.gradle.kts \
  app/auth/auth-core-api/build.gradle.kts \
  app/authlib/build.gradle.kts \
  app/compose/compose-ui/build.gradle.kts \
  app/core/core-build-constants/build.gradle.kts \
  app/core/core-common-public/build.gradle.kts \
  app/core/core-datastore-public/build.gradle.kts \
  app/core/core-locale/build.gradle.kts \
  app/core/core-markdown/build.gradle.kts \
  app/core/core-resources/build.gradle.kts \
  app/core/core-ui-data/build.gradle.kts \
  app/data/data-coinsured/build.gradle.kts \
  app/data/data-contract/build.gradle.kts \
  app/data/data-conversations/build.gradle.kts \
  app/data/data-termination/build.gradle.kts \
  app/datadog/datadog-core/build.gradle.kts \
  app/design-system/design-system-api/build.gradle.kts \
  app/design-system/design-system-hedvig/build.gradle.kts \
  app/design-system/design-system-internals/build.gradle.kts \
  app/feature/feature-choose-tier-navigation/build.gradle.kts \
  app/feature/feature-connect-payment-trustly-navigation/build.gradle.kts \
  app/feature/feature-edit-coinsured-navigation/build.gradle.kts \
  app/feature/feature-forever-navigation/build.gradle.kts \
  app/feature/feature-help-center-navigation/build.gradle.kts \
  app/feature/feature-help-center/build.gradle.kts \
  app/feature/feature-movingflow-navigation/build.gradle.kts \
  app/feature/feature-payin-account-navigation/build.gradle.kts \
  app/feature/feature-payments-navigation/build.gradle.kts \
  app/feature/feature-terminate-insurance-navigation/build.gradle.kts \
  app/feature/feature-travel-certificate-navigation/build.gradle.kts \
  app/featureflags/feature-flags/build.gradle.kts \
  app/language/language-core/build.gradle.kts \
  app/logging/logging-public/build.gradle.kts \
  app/molecule/molecule-public/build.gradle.kts \
  app/navigation/navigation-common/build.gradle.kts \
  app/navigation/navigation-compose/build.gradle.kts \
  app/navigation/navigation-core/build.gradle.kts \
  app/network/network-clients/build.gradle.kts \
  app/permission/permission-core/build.gradle.kts \
  app/shared/member-quick-actions/build.gradle.kts \
  app/shared/partners-deflect/build.gradle.kts \
  app/shareddi/build.gradle.kts \
  app/ui/placeholder/build.gradle.kts \
  app/ui/ui-emergency/build.gradle.kts \
  app/umbrella/build.gradle.kts; do
  sed -i '' 's/id("hedvig.multiplatform.library")/id("hedvig.multiplatform.library.ios")/' "$f"
done
```

- [ ] **Step 2: Move feature-claim-chat to the plain id**

Run: `sed -i '' 's/id("hedvig.multiplatform.library.jvm")/id("hedvig.multiplatform.library")/' app/feature/feature-claim-chat/build.gradle.kts`

- [ ] **Step 3: Update the graph kinds table**

In `gradle/projectDependencyGraph.gradle`, replace the first two `kinds` rows with:

```groovy
  [id: 'hedvig.multiplatform.library.ios', label: 'KMP, iOS targets', color: '#ffd2b3'],
  [id: 'hedvig.multiplatform.library',     label: 'KMP, no iOS',      color: '#fff3b3'],
```

- [ ] **Step 4: Assert the counts**

```bash
echo "ios: $(grep -l 'id("hedvig.multiplatform.library.ios")' $(find app -name build.gradle.kts -not -path '*/build/*') | wc -l)"
echo "plain: $(grep -l 'id("hedvig.multiplatform.library")' $(find app sample-module-kmp micro-apps -name build.gradle.kts -not -path '*/build/*') | wc -l)"
echo "jvm-id leftovers: $(grep -rl 'multiplatform.library.jvm' --include='*.kts' --include='*.kt' --include='*.md' --include='*.gradle' . | grep -v /build/ | wc -l)"
```
Expected: `ios: 47`, `plain: 20` (17 non-closure modules, `feature-claim-chat`, `sample-module-kmp`, `design-showcase-desktop`), `jvm-id leftovers: 0`.

- [ ] **Step 5: Update CLAUDE.md**

In the Convention Plugins list, replace the `hedvig.multiplatform.library` and `hedvig.multiplatform.library.android` bullets with:

```markdown
- **hedvig.multiplatform.library** - KMP module with a JVM target. Not exported to iOS.
- **hedvig.multiplatform.library.ios** - KMP module that `:umbrella` exports to iOS, directly or transitively. Adds the iOS targets; every `expect` needs a `nativeMain` actual.
- **hedvig.multiplatform.library.android** - adds an android target to a KMP module when it needs android-specific code, Compose previews, resources, or the `navKeys()`/`viewModels()` processors.
```

- [ ] **Step 6: Verify iOS still builds and Android still compiles**

```bash
./gradlew :umbrella:assembleHedvigSharedReleaseXCFramework -q
./gradlew :app:compileReleaseKotlin -q
```
Expected: both BUILD SUCCESSFUL. The XCFramework task is slow (several minutes). A failure naming a module that "does not have an iOS target" means that module belongs in the step 1 list; add it and re-run.

- [ ] **Step 7: Commit**

```bash
./gradlew ktlintFormat -q
git add -A app sample-module-kmp micro-apps gradle/projectDependencyGraph.gradle CLAUDE.md
git commit -m "build: opt the umbrella closure into iOS targets explicitly

The 46 modules :umbrella reaches, plus :umbrella itself, apply
hedvig.multiplatform.library.ios. feature-claim-chat moves to the plain id."
```

### Task 6: Delete native code outside the closure

**Files:**
- Delete: `app/audio-player-ui/src/nativeMain/`
- Delete: `app/compose/compose-photo-capture-state/src/nativeMain/`
- Delete: `app/compose/compose-result-launcher/src/nativeMain/`
- Delete: `app/core/core-file-upload/src/nativeMain/`

Each of the first three keeps its `expect` because the JVM and Android actuals differ (`CommonMediaPlayer`, `rememberPhotoCaptureState`, the two result-launcher functions). `core-file-upload` has no `expect`; its native files were standalone implementations.

- [ ] **Step 1: Remove the directories**

```bash
git rm -r -q app/audio-player-ui/src/nativeMain app/compose/compose-photo-capture-state/src/nativeMain app/compose/compose-result-launcher/src/nativeMain app/core/core-file-upload/src/nativeMain
find app -type d \( -name nativeMain -o -name iosMain \) -not -path '*/build/*' | sort
```
Expected: the listing shows only directories under closure modules (`authlib`, `core-build-constants`, `core-datastore-public`, `core-locale`, `core-ui-data`, `data-coinsured`, `design-system-hedvig`, `design-system-internals`, `feature-flags`, `feature-help-center`, `language-core`, `logging-public`, `molecule-public`, `network-clients`, `shareddi`).

- [ ] **Step 2: Compile the four modules on both remaining targets**

Run: `./gradlew :audio-player-ui:compileKotlinJvm :audio-player-ui:compileAndroidMain :compose-photo-capture-state:compileKotlinJvm :compose-photo-capture-state:compileAndroidMain :compose-result-launcher:compileKotlinJvm :compose-result-launcher:compileAndroidMain :core-file-upload:compileKotlinJvm :core-file-upload:compileAndroidMain -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git commit -m "build: drop native source sets from modules iOS never reaches"
```

### Task 7: Absorb per-module ceremony into the convention plugins

**Files:**
- Modify: `build-logic/convention/src/main/kotlin/com/hedvig/android/HedvigGradlePluginExtension.kt` (`ComposeHandler.configure`, `ViewModelsHandler.configure`)
- Modify: `build-logic/convention/src/main/kotlin/KotlinMultiplatformAndroidLibraryConventionPlugin.kt` (`configureKotlinAndroidMultiplatform`)
- Modify: `app/feature/feature-remove-addons/build.gradle.kts`, `app/feature/feature-help-center/build.gradle.kts`, `app/feature/feature-claim-history/build.gradle.kts`, `app/feature/feature-claim-chat/build.gradle.kts`, `app/design-system/design-system-hedvig/build.gradle.kts`, `app/shared/member-quick-actions/build.gradle.kts`

**Interfaces:**
- Produces: every KMP-android module with `compose()` gets `libs.bundles.kmpPreviewBugWorkaround` on `androidMain`; every KMP module with `viewModels()` gets `libs.metro.viewmodel` and `libs.metro.viewmodel.compose` on `commonMain`; every KMP-android module has the `jvmAndAndroid` source-set group.

- [ ] **Step 1: Preview workaround in ComposeHandler**

In `ComposeHandler.configure`, inside `if (isAndroidMultiplatformLibrary) {`, after the two `androidMainImplementation` lines add:

```kotlin
          add("androidMainImplementation", libs.bundles.kmpPreviewBugWorkaround)
```

- [ ] **Step 2: Metro ViewModel artifacts in ViewModelsHandler**

In `ViewModelsHandler.configure`, in the multiplatform branch, change the `project.dependencies { }` block to:

```kotlin
    project.dependencies {
      add("kspCommonMainMetadata", processor)
      add("kspAndroid", processor)
      // Android-library modules get these from configureMetro through the AGP plugin; a KMP module's
      // ViewModels live in commonMain, so the artifacts go there.
      add("commonMainImplementation", libs.metro.viewmodel)
      add("commonMainImplementation", libs.metro.viewmodel.compose)
    }
```

- [ ] **Step 3: Hierarchy group in the Android KMP plugin**

In `KotlinMultiplatformAndroidLibraryConventionPlugin.kt`, inside `project.configure<KotlinMultiplatformExtension> {` in `configureKotlinAndroidMultiplatform`, add as the first statement:

```kotlin
    // `jvmAndAndroidMain` is available to every module with both targets, for code that is shared by
    // the two JVM-backed platforms only (haptics, top app bars, anything with a native counterpart).
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
      common {
        group("jvmAndAndroid") {
          withAndroidLibraryTarget()
          withJvm()
        }
      }
    }
```

`ExperimentalKotlinGradlePluginApi` and `withAndroidLibraryTarget` are already in that file.

- [ ] **Step 4: Remove the declarations from modules**

In `feature-remove-addons`, `feature-help-center`, `feature-claim-history`, `feature-claim-chat`: delete the line `implementation(libs.bundles.kmpPreviewBugWorkaround)`; if that leaves an empty `androidMain.dependencies { }` block, delete the block.

In the same four modules: delete `implementation(libs.metro.viewmodel)` and `implementation(libs.metro.viewmodel.compose)` wherever they appear (`commonMain` in help-center, remove-addons and claim-chat; `androidMain` in claim-history). Leave `design-system-hedvig` and `shareddi` alone: they do not call `viewModels()` and need the artifact for `LocalMetroViewModelFactory`.

In `feature-help-center`, `design-system-hedvig`, `member-quick-actions`: delete the `applyDefaultHierarchyTemplate { common { group("jvmAndAndroid") { ... } } }` block together with its `@OptIn(ExperimentalKotlinGradlePluginApi::class)` line, and the now unused `import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi` at the top of each file.

Verify nothing is left:

```bash
grep -rn "kmpPreviewBugWorkaround\|group(\"jvmAndAndroid\")\|ExperimentalKotlinGradlePluginApi" $(find app -name build.gradle.kts -not -path '*/build/*')
grep -n "libs.metro.viewmodel" app/feature/feature-remove-addons/build.gradle.kts app/feature/feature-help-center/build.gradle.kts app/feature/feature-claim-history/build.gradle.kts app/feature/feature-claim-chat/build.gradle.kts
```
Expected: both empty.

- [ ] **Step 5: Compile the affected modules**

Run: `./gradlew :feature-help-center:compileAndroidMain :feature-help-center:compileKotlinJvm :feature-help-center:jvmTest :design-system-hedvig:compileAndroidMain :design-system-hedvig:compileKotlinJvm :member-quick-actions:compileAndroidMain :feature-claim-history:compileAndroidMain :feature-claim-chat:compileAndroidMain :feature-claim-chat:jvmTest :feature-remove-addons:compileAndroidMain -q`
Expected: BUILD SUCCESSFUL. `feature-help-center` compiling proves `jvmAndAndroidMain` is still a source root (its `RatingHaptic.kt` and `PuppyTopAppBar.kt` live there).

- [ ] **Step 6: Commit**

```bash
./gradlew ktlintFormat -q
git add -A build-logic app
git commit -m "build: move KMP module boilerplate into the convention plugins

The preview workaround bundle, the Metro ViewModel artifacts and the
jvmAndAndroid source-set group come from the plugins, so a converted module's
build file is the plugin ids, the hedvig block and commonMain dependencies."
```

### Task 8: Phase B verification and graph

- [ ] **Step 1: Full verification**

```bash
./gradlew :umbrella:assembleHedvigSharedReleaseXCFramework -q
./gradlew :app:assembleRelease -q
./gradlew jvmTest -q
```
Expected: all BUILD SUCCESSFUL.

- [ ] **Step 2: Regenerate the graph and check the counts moved**

```bash
./gradlew :generateProjectDependencyGraph --no-configuration-cache -q | head -12
```
Expected in the kind table: `KMP, iOS targets` 45 (47 minus the two excluded hubs), `KMP, no iOS` 20 (17 non-closure modules, `feature-claim-chat`, `sample-module-kmp`, `design-showcase-desktop`), closure still 47.

- [ ] **Step 3: Commit**

```bash
git add misc/images/modularization-graph.svg misc/images/modularization-summary.md
git commit -m "docs: regenerate the module graph after the iOS opt-in"
```

---

## Phase C: lint spike

No branch needed; nothing is committed unless outcome A applies.

### Task 9: Does AGP 9.4.1 lint KMP-android modules?

**Files:**
- Maybe modify: `build-logic/convention/src/main/kotlin/HedvigLintConventionPlugin.kt:25-28`
- Maybe modify: `docs/superpowers/specs/2026-10-12-kmp-readiness-migration-design.md` (Part 4, Lint)

- [ ] **Step 1: List lint tasks on three module shapes**

```bash
./gradlew :feature-help-center:tasks --all -q | grep -iE '^lint' ; echo "--- (KMP, android target)"
./gradlew :data-termination:tasks --all -q | grep -iE '^lint' ; echo "--- (KMP, no android target)"
./gradlew :feature-chip-id:tasks --all -q | grep -iE '^lint' ; echo "--- (android library, reference)"
```
Expected for the reference: `lint`, `lintRelease`, `lintAnalyzeRelease`, `updateLintBaseline...`. The question is whether the first two shapes list anything other than `lintKotlin*` (which is ktlint).

- [ ] **Step 2: If a `lint` task exists on the KMP shapes (outcome A)**

Run: `./gradlew :feature-help-center:lint -q` and open `app/feature/feature-help-center/build/reports/lint-results*.html`. If the report lists Compose or `hedvig-lint` checks, the configuration in `HedvigLintConventionPlugin` already works. Replace the four-line comment above `pluginManager.withPlugin(libs.plugins.androidLibraryMultiplatform...)` with:

```kotlin
      // The KMP-android variant's lint configuration is only reachable once the DSL is finalized.
```

Then run `./gradlew lint -q` at the root to confirm nothing new fails, and commit on a branch `kmp/lint-kmp-modules`:

```bash
git checkout -b kmp/lint-kmp-modules develop
git add build-logic
git commit -m "build: correct the lint comment on KMP-android modules"
```

- [ ] **Step 3: If no `lint` task exists (outcome B)**

Amend the spec's Part 4 Lint section by replacing the two-outcome bullets with:

```markdown
Spike result (AGP 9.4.1): no Android Lint task is registered on KMP-android modules or on KMP
modules without an Android target. Converted modules leave Android Lint coverage. Checks that
stop applying to them: the custom rules in `hedvig-lint`, and the Compose lint rules shipped
with `androidx.compose.ui:ui-lint` and `androidx.compose.runtime:runtime-lint`. `ktlint` still
runs on every module. Signed off on <date> by Stylianos Gakis before wave 1 started.
```

Commit on the spec branch:

```bash
git checkout kmp/readiness-migration-spec
git add docs/superpowers/specs/2026-10-12-kmp-readiness-migration-design.md
git commit -m "docs: record the lint spike outcome in the KMP migration spec"
```

Stop here and report the outcome; wave 1 starts only after the sign-off line is confirmed.

---

## Phase D: conversions

Branches: one per wave, each off `develop`. Phase B must have merged into `develop` first, or be cherry-picked onto each wave branch.

**Conversion recipe** (every conversion task below applies it; the task lists what differs):

1. Plugin ids: `hedvig.android.library` or `hedvig.jvm.library` becomes `hedvig.multiplatform.library`. Add `hedvig.multiplatform.library.android` on the line after it when the module calls `compose()`, `navKeys()`, `viewModels()` or `androidResources()`.
2. `git mv src/main/kotlin src/commonMain/kotlin`; `git mv src/main/graphql src/commonMain/graphql` when present; `git mv src/test src/jvmTest` when present; `git rm src/main/AndroidManifest.xml` (every candidate's manifest is the empty `<manifest />`).
3. Delete any `android { testOptions.unitTests.isReturnDefaultValues = true }` block.
4. Replace the `dependencies { }` block with `kotlin { sourceSets { commonMain.dependencies { } jvmTest.dependencies { } } }`. `implementation(x)` and `api(x)` lines go into `commonMain.dependencies` unchanged; `testImplementation(x)` lines go into `jvmTest.dependencies` as `implementation(x)`. Omit `jvmTest.dependencies` when there were no test dependencies.
5. Catalog swaps inside the moved lines: `libs.androidx.compose.foundation` to `libs.jetbrains.compose.foundation`; `libs.androidx.compose.foundationLayout` to `libs.jetbrains.compose.foundation.layout`; `libs.androidx.compose.runtime` to `libs.jetbrains.compose.runtime`; `libs.androidx.compose.ui` to `libs.jetbrains.compose.ui`. The `androidx.datastore`, `apollo`, `arrow`, `coroutines`, `kotlinx` and `metro` entries stay as they are.
6. Compile: `:module:compileKotlinJvm`, plus `:module:compileAndroidMain` when the android plugin is applied, plus `:module:jvmTest` when tests exist, then `:app:compileReleaseKotlin`.
7. `./gradlew ktlintFormat -q`, regenerate the graph, commit with the SVG and summary.

The target shape of every converted build file, with the module's own `hedvig { }` entries and its own dependency lines in place of the examples:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android") // only when rule 1 says so
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.arrow.core)
      implementation(projects.coreCommonPublic)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.junit)
    }
  }
}
```

### Task 10: Wave 0, delete the dead module

Branch: `kmp/wave-0` off `develop`.

**Files:**
- Delete: `app/data/data-product-variant/data-product-variant-android/`

- [ ] **Step 1: Confirm no consumers**

Run: `grep -rn "dataProductVariantAndroid\|productvariant.android" --include='*.kts' --include='*.kt' app | grep -v /build/ | grep -v data-product-variant-android/`
Expected: no output.

- [ ] **Step 2: Remove and verify**

```bash
git rm -r -q app/data/data-product-variant/data-product-variant-android
./gradlew :app:compileReleaseKotlin -q
./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
```
Expected: BUILD SUCCESSFUL; the summary's `Android library` count drops by one.

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: delete data-product-variant-android

Its one extension function has no callers."
```

### Task 11: Wave 1, compose-pager-indicator

Branch: `kmp/wave-1` off `develop` (Tasks 11 to 15 share it).

**Files:**
- Modify: `app/compose/compose-pager-indicator/build.gradle.kts`
- Move: `src/main/kotlin` to `src/commonMain/kotlin`; delete `src/main/AndroidManifest.xml`

- [ ] **Step 1: Move sources**

```bash
cd app/compose/compose-pager-indicator && git mv src/main/kotlin src/commonMain/kotlin && git rm -q src/main/AndroidManifest.xml && cd -
```

- [ ] **Step 2: Write the build file**

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(projects.designSystemHedvig)
    }
  }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew :compose-pager-indicator:compileKotlinJvm :compose-pager-indicator:compileAndroidMain :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app/compose/compose-pager-indicator misc/images
git commit -m "build(compose-pager-indicator): convert to KMP"
```

### Task 12: Wave 1, file-upload-ui

**Files:**
- Modify: `app/shared/file-upload-ui/build.gradle.kts`
- Move: `src/main/kotlin` to `src/commonMain/kotlin`; delete `src/main/AndroidManifest.xml`

- [ ] **Step 1: Move sources**

```bash
cd app/shared/file-upload-ui && git mv src/main/kotlin src/commonMain/kotlin && git rm -q src/main/AndroidManifest.xml && cd -
```

- [ ] **Step 2: Write the build file**

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.composeUi)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
    }
  }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew :file-upload-ui:compileKotlinJvm :file-upload-ui:compileAndroidMain :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app/shared/file-upload-ui misc/images
git commit -m "build(file-upload-ui): convert to KMP"
```

### Task 13: Wave 1, ui-phone-number

**Files:**
- Modify: `app/ui/ui-phone-number/build.gradle.kts`
- Move: `src/main/kotlin` to `src/commonMain/kotlin`; delete `src/main/AndroidManifest.xml` if present

- [ ] **Step 1: Move sources**

```bash
cd app/ui/ui-phone-number && git mv src/main/kotlin src/commonMain/kotlin && (test -f src/main/AndroidManifest.xml && git rm -q src/main/AndroidManifest.xml || true) && cd -
```

- [ ] **Step 2: Write the build file**

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      // api: the field's signature exposes TextFieldState, KeyboardActionHandler and
      // HedvigTextFieldDefaults, so a consumer cannot call it without seeing these.
      api(libs.jetbrains.compose.foundation)
      api(projects.coreCommonPublic)
      api(projects.designSystemHedvig)
    }
  }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew :ui-phone-number:compileKotlinJvm :ui-phone-number:compileAndroidMain :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app/ui/ui-phone-number misc/images
git commit -m "build(ui-phone-number): convert to KMP"
```

### Task 14: Wave 1, tier-comparison

**Files:**
- Modify: `app/shared/tier-comparison/build.gradle.kts`
- Move: `src/main/kotlin` to `src/commonMain/kotlin`; `src/main/graphql` to `src/commonMain/graphql`; delete `src/main/AndroidManifest.xml`

- [ ] **Step 1: Move sources**

```bash
cd app/shared/tier-comparison && git mv src/main/kotlin src/commonMain/kotlin && git mv src/main/graphql src/commonMain/graphql && git rm -q src/main/AndroidManifest.xml && cd -
```

- [ ] **Step 2: Write the build file**

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureFlags)
      implementation(projects.languageCore)
      implementation(projects.moleculePublic)
    }
  }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew :tier-comparison:compileKotlinJvm :tier-comparison:compileAndroidMain :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL. A "Unresolved reference" on an `octopus.` class means the graphql move in step 1 did not happen.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app/shared/tier-comparison misc/images
git commit -m "build(tier-comparison): convert to KMP"
```

### Task 15: Wave 1, feature-chip-id

**Files:**
- Modify: `app/feature/feature-chip-id/build.gradle.kts`
- Move: `src/main/kotlin` to `src/commonMain/kotlin`; `src/main/graphql` to `src/commonMain/graphql`; `src/test` to `src/jvmTest`; delete `src/main/AndroidManifest.xml`

- [ ] **Step 1: Move sources**

```bash
cd app/feature/feature-chip-id && git mv src/main/kotlin src/commonMain/kotlin && git mv src/main/graphql src/commonMain/graphql && git mv src/test src/jvmTest && git rm -q src/main/AndroidManifest.xml && cd -
```

- [ ] **Step 2: Write the build file**

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.coroutines.core)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.composeUi)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.coreUiData)
      implementation(projects.dataContract)
      implementation(projects.designSystemHedvig)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCore)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.junit)
    }
  }
}
```

- [ ] **Step 3: Compile and test**

Run: `./gradlew :feature-chip-id:compileKotlinJvm :feature-chip-id:compileAndroidMain :feature-chip-id:jvmTest :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL, tests pass.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app/feature/feature-chip-id misc/images
git commit -m "build(feature-chip-id): convert to KMP"
```

`core-icons` is not part of this wave. The spec's "Out of scope" section records why.

### Task 16: Wave 2a, JVM leaves

Branch: `kmp/wave-2a` off `develop`.

**Files:** the six build files below; each module's `src/main/kotlin` moves to `src/commonMain/kotlin`. None has tests.

- [ ] **Step 1: Move sources**

```bash
for d in app/auth/auth-event-core app/theme app/test/test-clock app/data/data-cross-sell-after-flow app/data/data-display-items app/navigation/navigation-activity; do
  git mv "$d/src/main/kotlin" "$d/src/commonMain/kotlin"
done
```

- [ ] **Step 2: Write the six build files**

`app/auth/auth-event-core/build.gradle.kts`, `app/theme/build.gradle.kts`, `app/navigation/navigation-activity/build.gradle.kts` (identical, no dependencies):

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}
```

`app/test/test-clock/build.gradle.kts`:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.kotlinx.datetime)
    }
  }
}
```

`app/data/data-cross-sell-after-flow/build.gradle.kts`:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.arrow.core)
      implementation(libs.coroutines.core)
      implementation(projects.coreCommonPublic)
    }
  }
}
```

`app/data/data-display-items/build.gradle.kts`:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.datetime)
    }
  }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew :auth-event-core:compileKotlinJvm :theme:compileKotlinJvm :test-clock:compileKotlinJvm :data-cross-sell-after-flow:compileKotlinJvm :data-display-items:compileKotlinJvm :navigation-activity:compileKotlinJvm :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Run the tests of their consumers**

Run: `./gradlew jvmTest test -q`
Expected: BUILD SUCCESSFUL (consumers such as `data-chat` depend on `test-clock` in main code and in tests).

- [ ] **Step 5: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app misc/images
git commit -m "build: convert the leaf JVM modules to KMP

auth-event-core, theme, test-clock, data-cross-sell-after-flow,
data-display-items, navigation-activity."
```

### Task 17: Room handler for KMP modules

Branch: `kmp/wave-2b` off `develop` (Task 18 shares it). This task lands first because `data-chat` and `database-core` need it.

**Files:**
- Modify: `build-logic/convention/src/main/kotlin/com/hedvig/android/HedvigGradlePluginExtension.kt` (`RoomHandler.configure`)

- [ ] **Step 1: Pick the KSP configuration by module shape**

Replace

```kotlin
    project.dependencies {
      val ksp = if (isTestOnly) "kspTest" else "ksp"
      add(ksp, libs.room.ksp)
    }
```

with

```kotlin
    project.dependencies {
      val isMultiplatform = project.extensions.findByType<KotlinMultiplatformExtension>() != null
      val ksp = when {
        isMultiplatform && isTestOnly -> "kspJvmTest"
        isMultiplatform -> "kspJvm"
        isTestOnly -> "kspTest"
        else -> "ksp"
      }
      add(ksp, libs.room.ksp)
    }
```

- [ ] **Step 2: Compile build-logic and the existing Room modules**

Run: `./gradlew -p build-logic :convention:build -q && ./gradlew :data-chat:compileKotlin :database-core:compileKotlin :database-android:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL (the non-KMP path is unchanged).

- [ ] **Step 3: Commit**

```bash
git add build-logic
git commit -m "build: wire Room's KSP processor to the JVM target on KMP modules"
```

### Task 18: Wave 2b, remaining JVM modules

**Files:** the fifteen build files below. Source moves: `src/main/kotlin` to `src/commonMain/kotlin` for all; `src/main/graphql` to `src/commonMain/graphql` for the modules with `apollo(...)`; `src/test` to `src/jvmTest` for the modules with a `test` directory (`data-settings-datastore-public`, `data-addons`, `data-paying-member`, `notification-badge-data-public`, `language-data`, `data-chat`, `data-changetier`, `auth-core-public`, `member-reminders-public`).

- [ ] **Step 1: Move sources**

```bash
for d in app/core/core-demo-mode app/data/data-settings-datastore/data-settings-datastore-public app/data/data-addons app/data/data-paying-member app/data/data-cross-sell-after-claim-closed app/core/core-app-review app/apollo/apollo-auth-listeners app/datadog/datadog-demo-tracking app/notification-badge-data/notification-badge-data-public app/language/language-data app/data/data-chat app/database/database-core app/data/data-changetier app/auth/auth-core-public app/member-reminders/member-reminders-public; do
  git mv "$d/src/main/kotlin" "$d/src/commonMain/kotlin"
  test -d "$d/src/main/graphql" && git mv "$d/src/main/graphql" "$d/src/commonMain/graphql"
  test -d "$d/src/test" && git mv "$d/src/test" "$d/src/jvmTest"
done
for d in app/core/core-demo-mode app/data/data-settings-datastore/data-settings-datastore-public app/data/data-addons app/data/data-paying-member app/data/data-cross-sell-after-claim-closed app/core/core-app-review app/apollo/apollo-auth-listeners app/datadog/datadog-demo-tracking app/notification-badge-data/notification-badge-data-public app/language/language-data app/data/data-chat app/database/database-core app/data/data-changetier app/auth/auth-core-public app/member-reminders/member-reminders-public; do
  test -d "$d/src/main" && echo "LEFTOVER $d/src/main"
done
```
Expected: the final loop prints nothing (no `src/main` left in these modules).

- [ ] **Step 2: Replace the UUID import in two modules**

```bash
grep -rln "java.util.UUID" app/auth/auth-core-public/src app/member-reminders/member-reminders-public/src
```
In each file listed, replace `import java.util.UUID` with `import com.benasher44.uuid.uuid4`, and each `UUID.randomUUID().toString()` with `uuid4().toString()`. If a file uses `UUID` any other way, stop and drop that module from the wave (note it in the commit message).

- [ ] **Step 3: Write the build files**

Every file starts with

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}
```

followed by the module's existing `hedvig { }` block unchanged, then `kotlin { sourceSets { commonMain.dependencies { ... } jvmTest.dependencies { ... } } }` built by the recipe from the module's current `dependencies { }` block: `implementation(...)`/`api(...)` lines copied verbatim into `commonMain.dependencies`, `testImplementation(x)` lines copied into `jvmTest.dependencies` as `implementation(x)`. Additions on top of that:

- `auth-core-public`: add `implementation(libs.uuid)` to `commonMain.dependencies`.
- `member-reminders-public`: add `implementation(libs.uuid)` to `commonMain.dependencies`.
- `data-chat`: keep `room(isTestOnly = true) { resolve("app/data/data-chat/build/generated/ksp/test/kotlin") }` but change the resolved path to `"app/data/data-chat/build/generated/ksp/jvm/jvmTest/kotlin"`. That directory is only the schema export location for the test-only database, so any writable path under `build/` works.
- `database-core`: `room { resolve("app/database/schemas") }` unchanged.

Fully written example, `app/core/core-demo-mode/build.gradle.kts`:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.androidx.datastore.core)
      implementation(libs.androidx.datastore.preferencesCore)
      implementation(libs.coroutines.core)
      implementation(projects.authEventCore)
      implementation(projects.coreCommonPublic)
    }
  }
}
```

Fully written example with tests and Apollo, `app/data/data-paying-member/build.gradle.kts`:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.arrow.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDemoMode)
      implementation(projects.dataContract)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.junit)
    }
  }
}
```

- [ ] **Step 4: Compile and test, leaves first**

```bash
./gradlew :core-demo-mode:compileKotlinJvm :data-settings-datastore-public:compileKotlinJvm :data-addons:compileKotlinJvm :data-paying-member:compileKotlinJvm :data-cross-sell-after-claim-closed:compileKotlinJvm :core-app-review:compileKotlinJvm :apollo-auth-listeners:compileKotlinJvm :datadog-demo-tracking:compileKotlinJvm :notification-badge-data-public:compileKotlinJvm :language-data:compileKotlinJvm :auth-core-public:compileKotlinJvm -q
./gradlew :data-chat:compileKotlinJvm :database-core:compileKotlinJvm :data-changetier:compileKotlinJvm :member-reminders-public:compileKotlinJvm -q
./gradlew :data-settings-datastore-public:jvmTest :data-addons:jvmTest :data-paying-member:jvmTest :notification-badge-data-public:jvmTest :language-data:jvmTest :data-chat:jvmTest :data-changetier:jvmTest :auth-core-public:jvmTest :member-reminders-public:jvmTest -q
./gradlew :app:compileReleaseKotlin -q
```
Expected: all BUILD SUCCESSFUL. A failure in one module: revert that module's changes (`git checkout develop -- <module dir>` after restoring its moved directories), drop it from the wave, continue, and name it in the commit message. A failure in `data-chat`'s `jvmTest` mentioning a missing generated Room class means the `kspJvmTest` wiring from Task 17 did not apply; check the `room(isTestOnly = true)` path.

- [ ] **Step 5: Full test run**

Run: `./gradlew jvmTest test -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app misc/images
git commit -m "build: convert the remaining JVM library modules to KMP

core-demo-mode, data-settings-datastore-public, data-addons,
data-paying-member, data-cross-sell-after-claim-closed, core-app-review,
apollo-auth-listeners, datadog-demo-tracking, notification-badge-data-public,
language-data, data-chat, database-core, data-changetier, auth-core-public,
member-reminders-public. The two java.util.UUID uses move to the
multiplatform uuid library."
```

### Task 19: Wave 3a, claim-status, cross-sells, feature-delete-account

Branch: `kmp/wave-3a` off `develop`.

- [ ] **Step 1: Move sources**

```bash
for d in app/ui/claim-status app/ui/cross-sells app/feature/feature-delete-account; do
  git mv "$d/src/main/kotlin" "$d/src/commonMain/kotlin"
  test -d "$d/src/main/graphql" && git mv "$d/src/main/graphql" "$d/src/commonMain/graphql"
  test -d "$d/src/test" && git mv "$d/src/test" "$d/src/jvmTest"
  git rm -q "$d/src/main/AndroidManifest.xml"
done
```

- [ ] **Step 2: Write the build files**

`app/ui/claim-status/build.gradle.kts`:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      // copy every implementation(...) / api(...) line from the current dependencies block,
      // with libs.androidx.compose.foundationLayout replaced by libs.jetbrains.compose.foundation.layout
    }
  }
}
```

`app/ui/cross-sells/build.gradle.kts`: same shape as `claim-status`, same catalog swap.

`app/feature/feature-delete-account/build.gradle.kts`:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      // copy every implementation(...) line from the current dependencies block unchanged
      // (androidx.datastore entries stay)
    }
  }
}
```

For every file, "copy" means the lines as they stand in the module's `build.gradle.kts` on `develop`, in the same order; no line is added or removed except the catalog swap named above.

- [ ] **Step 3: Compile and test**

Run: `./gradlew :claim-status:compileKotlinJvm :claim-status:compileAndroidMain :cross-sells:compileKotlinJvm :cross-sells:compileAndroidMain :feature-delete-account:compileKotlinJvm :feature-delete-account:compileAndroidMain :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app misc/images
git commit -m "build: convert claim-status, cross-sells and feature-delete-account to KMP"
```

### Task 20: Wave 3b, ui-analytics-consent, feature-payout-account

Branch: `kmp/wave-3b` off `develop`.

- [ ] **Step 1: Move sources**

```bash
for d in app/ui/ui-analytics-consent app/feature/feature-payout-account; do
  git mv "$d/src/main/kotlin" "$d/src/commonMain/kotlin"
  test -d "$d/src/main/graphql" && git mv "$d/src/main/graphql" "$d/src/commonMain/graphql"
  test -d "$d/src/test" && git mv "$d/src/test" "$d/src/jvmTest"
  test -f "$d/src/main/AndroidManifest.xml" && git rm -q "$d/src/main/AndroidManifest.xml"
done
```

- [ ] **Step 2: Write the build files**

`ui-analytics-consent`: plugins (base, android, gradle), `hedvig { compose() }`, no `android { }` block, `commonMain.dependencies` with the current implementation lines and `libs.androidx.compose.foundation` swapped to `libs.jetbrains.compose.foundation`, `jvmTest.dependencies` with `implementation(libs.assertK)`, `implementation(libs.coroutines.test)`, `implementation(libs.junit)`, `implementation(libs.turbine)`.

`feature-payout-account`: plugins (base, android, gradle), `hedvig { apollo("octopus") serialization() compose() navKeys() viewModels() }`, `commonMain.dependencies` with the current implementation lines unchanged.

- [ ] **Step 3: Compile and test**

Run: `./gradlew :ui-analytics-consent:compileKotlinJvm :ui-analytics-consent:compileAndroidMain :ui-analytics-consent:jvmTest :feature-payout-account:compileKotlinJvm :feature-payout-account:compileAndroidMain :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL. `ui-analytics-consent`'s tests previously ran with `isReturnDefaultValues`; a failure that mentions an `android.` class means those tests touch the framework and the module is dropped from the wave.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app misc/images
git commit -m "build: convert ui-analytics-consent and feature-payout-account to KMP"
```

### Task 21: Wave 3c, feature-choose-tier, feature-addon-purchase, feature-cross-sell-sheet

Branch: `kmp/wave-3c` off `develop`.

- [ ] **Step 1: Move sources**

```bash
for d in app/feature/feature-choose-tier app/feature/feature-addon-purchase app/feature/feature-cross-sell-sheet; do
  git mv "$d/src/main/kotlin" "$d/src/commonMain/kotlin"
  git mv "$d/src/main/graphql" "$d/src/commonMain/graphql"
  git mv "$d/src/test" "$d/src/jvmTest"
  git rm -q "$d/src/main/AndroidManifest.xml"
done
```

- [ ] **Step 2: Write the build files**

All three: plugins (base, android, gradle); `hedvig { }` block copied unchanged (`feature-cross-sell-sheet` has `compose() apollo("octopus") viewModels()`, the other two have `apollo("octopus") serialization() compose() navKeys() viewModels()`); the `android { testOptions ... }` block deleted; `commonMain.dependencies` with the current implementation lines unchanged; `jvmTest.dependencies` with the current `testImplementation` lines as `implementation`.

- [ ] **Step 3: Compile and test**

Run: `./gradlew :feature-choose-tier:compileAndroidMain :feature-choose-tier:jvmTest :feature-addon-purchase:compileAndroidMain :feature-addon-purchase:jvmTest :feature-cross-sell-sheet:compileAndroidMain :feature-cross-sell-sheet:jvmTest :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app misc/images
git commit -m "build: convert feature-choose-tier, feature-addon-purchase and feature-cross-sell-sheet to KMP"
```

### Task 22: Wave 3d, feature-insurances

Branch: `kmp/wave-3d` off `develop`.

- [ ] **Step 1: Move sources**

```bash
d=app/feature/feature-insurances
git mv "$d/src/main/kotlin" "$d/src/commonMain/kotlin" && git mv "$d/src/main/graphql" "$d/src/commonMain/graphql" && git mv "$d/src/test" "$d/src/jvmTest" && git rm -q "$d/src/main/AndroidManifest.xml"
```

- [ ] **Step 2: Write the build file**

Plugins (base, android, gradle); `hedvig { apollo("octopus") serialization() compose() navKeys() viewModels() }`; `android { }` block deleted; `commonMain.dependencies` with the current implementation lines unchanged; `jvmTest.dependencies` with `implementation(libs.assertK)`, `implementation(libs.coroutines.test)`, `implementation(libs.junit)`, `implementation(libs.testParameterInjector)`, `implementation(libs.turbine)` plus any `testImplementation(projects.*Test)` lines as `implementation`.

- [ ] **Step 3: Compile and test**

Run: `./gradlew :feature-insurances:compileAndroidMain :feature-insurances:jvmTest :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app misc/images
git commit -m "build: convert feature-insurances to KMP"
```

### Task 23: Wave 3e, feature-movingflow, feature-edit-coinsured

Branch: `kmp/wave-3e` off `develop`.

- [ ] **Step 1: Replace the two platform calls**

```bash
grep -rn "isDigitsOnly" app/feature/feature-movingflow/src
grep -rn "java.util.UUID\|UUID\." app/feature/feature-edit-coinsured/src
```
In `feature-movingflow`, replace `import androidx.core.text.isDigitsOnly` and each `x.isDigitsOnly()` with `x.all { it.isDigit() }`. In `feature-edit-coinsured`, replace `import java.util.UUID` with `import com.benasher44.uuid.uuid4` and `UUID.randomUUID().toString()` with `uuid4().toString()`. Any other use of either API: stop, drop that module from the wave, note it in the commit.

- [ ] **Step 2: Move sources**

```bash
for d in app/feature/feature-movingflow app/feature/feature-edit-coinsured; do
  git mv "$d/src/main/kotlin" "$d/src/commonMain/kotlin"
  git mv "$d/src/main/graphql" "$d/src/commonMain/graphql"
  test -d "$d/src/test" && git mv "$d/src/test" "$d/src/jvmTest"
  git rm -q "$d/src/main/AndroidManifest.xml"
done
```

- [ ] **Step 3: Write the build files**

`feature-movingflow`: plugins (base, android, gradle); `hedvig { apollo(packageName = "octopus.feature.movingflow") serialization() compose() navKeys() viewModels() }`; `commonMain.dependencies` with the current implementation lines unchanged.

`feature-edit-coinsured`: plugins (base, android, gradle); `hedvig { apollo("octopus") serialization() compose() navKeys() viewModels() }`; `android { }` block deleted; `commonMain.dependencies` with the current implementation lines, `libs.androidx.compose.foundation` swapped to `libs.jetbrains.compose.foundation`, plus `implementation(libs.uuid)`; `jvmTest.dependencies` with the current `testImplementation` lines as `implementation`.

- [ ] **Step 4: Compile and test**

Run: `./gradlew :feature-movingflow:compileAndroidMain :feature-movingflow:compileKotlinJvm :feature-edit-coinsured:compileAndroidMain :feature-edit-coinsured:jvmTest :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app misc/images
git commit -m "build: convert feature-movingflow and feature-edit-coinsured to KMP

isDigitsOnly and java.util.UUID give way to their Kotlin equivalents."
```

### Task 24: Wave 4, finish feature-claim-history

Branch: `kmp/wave-4` off `develop`.

**Files:**
- Move: `app/feature/feature-claim-history/src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryDestination.kt` and `ClaimHistoryViewModel.kt` to the same path under `src/commonMain/kotlin`
- Keep: `src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/nav/ClaimHistoryDestination.kt`
- Modify: `app/feature/feature-claim-history/build.gradle.kts`

- [ ] **Step 1: Move the two files**

```bash
d=app/feature/feature-claim-history/src
git mv $d/androidMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryDestination.kt $d/commonMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryDestination.kt
git mv $d/androidMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryViewModel.kt $d/commonMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryViewModel.kt
```

- [ ] **Step 2: Move the dependencies the screens need**

In the build file, move `implementation(projects.coreResources)`, `implementation(projects.designSystemHedvig)` and `implementation(projects.moleculePublic)` from `androidMain.dependencies` to `commonMain.dependencies`. Leave `projects.navigationCommon` and `projects.navigationCompose` in `androidMain.dependencies`, since only the nav entry uses them. The resulting file:

```kotlin
plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.api)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.moleculePublic)
    }
    androidMain.dependencies {
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
    }
  }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew :feature-claim-history:compileKotlinJvm :feature-claim-history:compileAndroidMain :app:compileReleaseKotlin -q`
Expected: BUILD SUCCESSFUL. An unresolved import in the moved files points at a dependency that also has to move to `commonMain`; move it and re-run.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q && ./gradlew :generateProjectDependencyGraph --no-configuration-cache -q > /dev/null
git add -A app/feature/feature-claim-history misc/images
git commit -m "build(feature-claim-history): move the screen and ViewModel to commonMain"
```

### Task 25: Final graph and README counts

Branch: `kmp/graph-after` off `develop`, after every wave has merged.

- [ ] **Step 1: Regenerate**

Run: `./gradlew :generateProjectDependencyGraph --no-configuration-cache -q`
Expected: the kind table shows roughly `KMP, iOS targets` 45, `KMP, no iOS` 55 or more, `Android library` 25 or fewer, `JVM library` 0 or a handful of test-only leftovers.

- [ ] **Step 2: Add the before and after line to the README**

Under the Module structure paragraph add:

```markdown
Before the 2026 KMP-readiness migration the graph had 63 KMP modules with iOS targets, 1 without, 47 Android
libraries and 35 JVM libraries; the current counts are in the summary file.
```

- [ ] **Step 3: Commit**

```bash
git add README.md misc/images
git commit -m "docs: regenerate the module graph after the KMP-readiness migration"
```
