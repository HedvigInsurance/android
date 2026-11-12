package com.hedvig.android.desktop.walkthrough

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.semantics.Role
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.desktop.DesktopAppBuildConfig
import com.hedvig.android.desktop.DesktopBackend
import com.hedvig.android.desktop.DesktopGraph
import com.hedvig.android.desktop.installDesktopLogging
import com.hedvig.android.desktop.newBackstackController
import com.hedvig.android.desktop.registerSharedEntries
import com.hedvig.android.desktop.registeredKeyClasses
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import dev.zacsweers.metro.createGraphFactory
import java.io.File
import java.util.Collections
import java.util.Locale
import java.util.TimeZone
import kotlin.io.path.createTempDirectory
import kotlin.reflect.KClass
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Renders the real desktop app off-screen, visits every screen and writes a PNG per screen and an `index.md` of
 * outcomes. Run it with `./gradlew :app-desktop:desktopWalkthrough`; it is skipped everywhere else.
 *
 * Gradle properties, all optional:
 * - `-Phedvig.walkthrough.mode=demo|session` (default `demo`).
 * - `-Phedvig.walkthrough.outputDir=<path>` (default `app/app-desktop/build/walkthrough`).
 * - `-Phedvig.walkthrough.dataDir=<path>`: demo mode only, the data directory; default a fresh temporary one.
 * - `-Phedvig.walkthrough.sessionDataDir=<path>`: session mode only, a logged-in desktop data directory. It is
 *   copied to a temporary directory and only the copy is used.
 * - `-Phedvig.walkthrough.only=<text>`: only visit keys whose name contains the text.
 *
 * Demo mode turns on the app's demo mode, which is what the login screen's demo option does, so no account is used.
 * Screens without demo data call the staging backend unauthenticated and show its errors.
 *
 * Session mode shows real staging data. It only pushes keys and clicks the navigation bar, and never pushes a key
 * whose screen writes to the backend on entry (see [sessionModeExclusions]). It refuses to start while a desktop app
 * process runs, since both would refresh the same session. Even run alone, a token refresh in the copy can rotate the
 * refresh token, so the desktop app the copy was taken from may have to log in again afterwards.
 */
class DesktopWalkthrough {
  private val spec = SceneSpec()
  private val logger = WalkthroughLogger()
  private val uncaught: MutableList<Throwable> = Collections.synchronizedList(mutableListOf())
  private val notices: MutableList<String> = Collections.synchronizedList(mutableListOf())
  private val visits = mutableListOf<Visit>()
  private lateinit var outputDirectory: File
  private lateinit var graph: DesktopGraph
  private lateinit var backstackController: BackstackController
  private var scene: WalkthroughScene? = null

  @Test
  fun walkthrough() {
    assumeTrue("Run with ./gradlew :app-desktop:desktopWalkthrough", System.getProperty("hedvig.walkthrough") == "true")
    val mode = System.getProperty("hedvig.walkthrough.mode") ?: "demo"
    require(mode == "demo" || mode == "session") { "hedvig.walkthrough.mode must be demo or session, was $mode" }
    Locale.setDefault(Locale.forLanguageTag("en-SE"))
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"))
    installDesktopLogging(logger)
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      println("Uncaught on ${thread.name}: ${throwable.firstFrames()}")
      uncaught += throwable
    }
    outputDirectory = File(System.getProperty("hedvig.walkthrough.outputDir") ?: "build/walkthrough")
    outputDirectory.deleteRecursively()
    outputDirectory.mkdirs()
    val dataDirectory = if (mode == "session") sessionDataDirectory() else demoDataDirectory()
    println("Walkthrough in $mode mode, data directory ${dataDirectory.absolutePath}")

    backstackController = newBackstackController()
    backstackController.seedIfEmpty(listOf(LoginKey))
    val (versionName, versionCode) = androidVersion()
    graph = createGraphFactory<DesktopGraph.Factory>().create(
      backstackController,
      DesktopAppBuildConfig(DesktopBackend.Staging, versionName = versionName, versionCode = versionCode),
      dataDirectory,
    )
    graph.initializables.forEach { it.initialize() }

    try {
      openScene()
      if (mode == "demo") {
        visitCurrent("start-logged-out", "start")
        enterDemoMode()
      }
      visitCurrent("start-logged-in", "start")
      visitTabs()
      visitKeys(mode)
    } finally {
      closeScene()
      val header = listOf(
        "Mode: $mode ($demoEntryNote)",
        "Scene: ${spec.widthDp} x ${spec.heightDp} dp at density ${spec.density}, locale en-SE, zone Europe/Stockholm",
        "Settle: idle after ${spec.quietMillis} ms without invalidation, or settled after ${spec.settledMillis} ms " +
          "of unchanged semantics with no spinner, timeout ${spec.timeoutMillis} ms",
        "Notices shown by the app: ${notices.distinct().ifEmpty { listOf("none") }.joinToString(" / ")}",
      )
      writeIndex(File(outputDirectory, "index.md"), header, visits)
      println("Wrote ${visits.size} visits to ${outputDirectory.absolutePath}")
      Outcome.entries.forEach { outcome -> println("${outcome.label}: ${visits.count { it.outcome == outcome }}") }
    }
  }

  /**
   * Enters demo mode the way a member does: Login, then a long press on the BankID QR code, then Yes in the dialog.
   * Each step is recorded as a visit. When a step cannot be found, demo mode is turned on directly through
   * [com.hedvig.android.core.demomode.DemoManager] and the back stack is set to logged in, which is what the
   * confirmation and the login screen do, and the fallback is noted on the visit.
   */
  private fun enterDemoMode() {
    val scene = scene ?: error("No scene")
    val failure = run {
      val login = scene.visibleNodes().firstOrNull { it.role == Role.Button && it.text == "Login" }
        ?: return@run "no Login button on the start screen"
      scene.click(login.bounds)
      visitCurrent("demo-step-1-bankid-login", "demo entry", listOf("clicked Login"))
      val qrCode = scene.visibleNodes().firstOrNull { it.testTag == "qr_code" }
        ?: return@run "no qr_code node on the BankID screen"
      scene.longPress(qrCode.bounds)
      visitCurrent("demo-step-2-start-demo-dialog", "demo entry", listOf("long-pressed the QR code"))
      val yes = scene.visibleNodes().lastOrNull { it.role == Role.Button && it.text == "Yes" }
        ?: return@run "no Yes button in the demo dialog"
      scene.click(yes.bounds)
      scene.settle()
      if (!onEdt { backstackController.isLoggedIn }) return@run "confirming demo mode did not log in"
      null
    }
    if (failure != null) {
      println("Entering demo mode through the UI failed: $failure. Turning it on directly.")
      runBlocking { graph.demoManager.setDemoMode(true) }
      onEdt { backstackController.setLoggedIn(null) }
      demoEntryNote = "demo mode turned on directly: $failure"
    } else {
      demoEntryNote = "demo mode entered through the BankID screen's demo dialog"
    }
  }

  private var demoEntryNote: String = "not demo mode"

  private fun openScene() {
    closeScene()
    scene = WalkthroughScene(spec) {
      WalkthroughApp(graph, backstackController, spec, onNotice = { notices += it })
    }
  }

  private fun closeScene() {
    scene?.close()
    scene = null
  }

  /** Settles the current screen and records it as a visit named [name]. */
  private fun visitCurrent(name: String, kind: String, notes: List<String> = emptyList()): Visit {
    logger.drain()
    uncaught.clear()
    val scene = scene ?: error("No scene")
    val settle = scene.settle()
    return record(name, kind, settle, notes)
  }

  private fun record(name: String, kind: String, settle: SettleResult, notes: List<String>): Visit {
    val scene = scene ?: error("No scene")
    val problems = logger.drain()
    val throwables = listOfNotNull(settle.renderFailure) + uncaught.toList()
    uncaught.clear()
    val nodes = if (settle.renderFailure ==
      null
    ) {
      runCatching { scene.visibleNodes() }.getOrDefault(emptyList())
    } else {
      emptyList()
    }
    val texts = nodes.map { it.text }
    val png = "%03d-%s.png".format(visits.size + 1, name)
    val captured = settle.renderFailure == null && runCatching { scene.capture(File(outputDirectory, png)) }.isSuccess
    val outcome = when {
      throwables.isNotEmpty() -> Outcome.Threw
      problems.any { it.message.startsWith("No entry is registered for") } -> Outcome.Fallback
      texts.any { it.contains("Not available on desktop") } -> Outcome.NotAvailable
      settle.settle == Settle.TimedOut -> Outcome.TimedOut
      else -> Outcome.Rendered
    }
    val extraNotes = buildList {
      addAll(notes)
      if (texts.any { it.contains("Something went wrong") }) add("shows the generic error")
      val untranslated = texts.flatMap { it.split(" ") }.filter { it.matches(Regex("[A-Z][A-Z0-9]*_[A-Z0-9_]+")) }
      if (untranslated.isNotEmpty()) add("untranslated keys: ${untranslated.distinct().joinToString()}")
      add("top: ${backstackController.entries.lastOrNull()?.let { it::class.simpleName }}")
    }
    val visit = Visit(
      name = name,
      kind = kind,
      outcome = outcome,
      settle = settle,
      notes = extraNotes,
      failure = throwables.takeIf { it.isNotEmpty() }?.joinToString("\n") { it.firstFrames() },
      problems = problems,
      png = png.takeIf { captured },
    )
    visits += visit
    File(outputDirectory, png.removeSuffix(".png") + ".txt").writeText(texts.joinToString("\n"))
    println("Visit ${visits.size} $name: ${outcome.label} after ${settle.millis} ms (${settle.settle})")
    return visit
  }

  private fun visitTabs() {
    val scene = scene ?: return
    val tabCount = scene.visibleNodes().count { it.role == Role.Tab }
    if (tabCount == 0) {
      visits +=
        Visit("tabs", "tab", Outcome.NeedsData, null, listOf("no navigation bar is shown"), null, emptyList(), null)
      return
    }
    for (index in 0 until tabCount) {
      val tab = scene.visibleNodes().filter { it.role == Role.Tab }.sortedBy { it.bounds.left }.getOrNull(index)
        ?: continue
      logger.drain()
      uncaught.clear()
      scene.click(tab.bounds)
      val settle = scene.settle()
      record(
        "tab-${backstackController.currentTopLevel}",
        "tab",
        settle,
        listOf("clicked tab ${index + 1}: ${tab.text}"),
      )
      if (settle.renderFailure != null) reopenAt(TopLevelTab.Home)
    }
    onEdt { backstackController.selectTopLevel(TopLevelTab.Home) }
    this.scene?.settle()
  }

  private fun reopenAt(tab: TopLevelTab) {
    openScene()
    onEdt { backstackController.selectTopLevel(tab) }
    scene?.settle()
  }

  private fun visitKeys(mode: String) {
    val only = System.getProperty("hedvig.walkthrough.only")
    val ids = if (mode == "demo") demoIds else emptyMap()
    val keyClasses = registeredKeyClasses().sortedBy { it.qualifiedName }
      .filter { only == null || it.qualifiedName.orEmpty().contains(only) }
    val baseEntries = onEdt { backstackController.entries.toList() }
    val baseParked = onEdt { backstackController.parkedRuns.toMap() }
    for (keyClass in keyClasses) {
      val name = keyClass.qualifiedName!!.removePrefix("com.hedvig.android.").removePrefix("feature.")
      val exclusion = if (mode == "session") {
        sessionModeExclusions.entries.firstOrNull { (prefix, _) ->
          keyClass.qualifiedName!!.startsWith(prefix)
        }
      } else {
        null
      }
      if (exclusion != null) {
        visits += Visit(name, "key", Outcome.Excluded, null, listOf(exclusion.value), null, emptyList(), null)
        continue
      }
      when (val instance = buildKey(keyClass, ids)) {
        is KeyInstance.NeedsData -> {
          visits += Visit(name, "key", Outcome.NeedsData, null, listOf(instance.reason), null, emptyList(), null)
        }

        is KeyInstance.Built -> {
          logger.drain()
          uncaught.clear()
          onEdt { Snapshot.withMutableSnapshot { backstackController.entries.add(instance.key) } }
          val settle = scene!!.settle()
          val notes = instance.fakedArguments.map { "arg $it" }
          val visit = record(name, "key", settle, notes)
          if (visit.outcome == Outcome.Threw) {
            openScene()
          }
          onEdt {
            Snapshot.withMutableSnapshot {
              backstackController.entries.clear()
              backstackController.entries.addAll(baseEntries)
              backstackController.parkedRuns.clear()
              backstackController.parkedRuns.putAll(baseParked)
            }
          }
          scene!!.settle(minimumMillis = 0)
          logger.drain()
          uncaught.clear()
        }
      }
    }
  }

  /** Every key class the shared entry provider registers. */
  private fun registeredKeyClasses(): List<KClass<*>> {
    val scope = EntryProviderScope<HedvigNavKey> { key -> error("No entry is registered for $key") }
    registerSharedEntries(scope, graph)
    return scope.registeredKeyClasses().toList()
  }

  private fun demoDataDirectory(): File {
    val given = System.getProperty("hedvig.walkthrough.dataDir")?.let(::File)
      ?: return createTempDirectory("hedvig-walkthrough-demo").toFile()
    require(!given.canonicalFile.startsWith(realDataRoot().canonicalFile)) {
      "The walkthrough never uses the desktop app's own data directory ${realDataRoot()}"
    }
    return given.apply { mkdirs() }
  }

  private fun sessionDataDirectory(): File {
    val source = System.getProperty("hedvig.walkthrough.sessionDataDir")?.let(::File)
      ?: error("Session mode needs -Phedvig.walkthrough.sessionDataDir=<a logged-in desktop data directory>")
    require(source.isDirectory) { "$source is not a directory" }
    val running = runningDesktopApps()
    check(running.isEmpty()) {
      "A desktop app is running (${running.joinToString()}). Session mode refreshes the same session, which can " +
        "log that app out, so it does not start while one runs."
    }
    val copy = createTempDirectory("hedvig-walkthrough-session").toFile()
    source.copyRecursively(copy, overwrite = true)
    return copy
  }

  /** The pids of running processes whose command line starts the desktop app. */
  private fun runningDesktopApps(): List<Long> {
    val self = ProcessHandle.current().pid()
    val fromHandles = ProcessHandle.allProcesses()
      .filter { it.pid() != self }
      .filter { handle -> handle.info().commandLine().orElse("").contains(DESKTOP_MAIN_CLASS) }
      .map { it.pid() }
      .toList()
    val fromPs = runCatching {
      ProcessBuilder("ps", "-axo", "pid=,command=").start().inputStream.bufferedReader().readLines()
        .filter { it.contains(DESKTOP_MAIN_CLASS) && !it.contains(" ps ") }
        .mapNotNull { it.trim().substringBefore(" ").toLongOrNull() }
        .filter { it != self }
    }.getOrDefault(emptyList())
    return (fromHandles + fromPs).distinct()
  }

  private fun realDataRoot(): File = File(System.getProperty("user.home"), ".hedvig-desktop")

  /** The Android app's `versionName` and `versionCode`, which the backend and Unleash judge the desktop app by. */
  private fun androidVersion(): Pair<String, Int> {
    val script = generateSequence(File("").absoluteFile) { it.parentFile }
      .map { File(it, "app/app/build.gradle.kts") }
      .firstOrNull { it.isFile }
      ?.readText()
      ?: return "0.0.0" to 0
    val name = Regex("""^ *versionName = "([0-9.]+)"$""", RegexOption.MULTILINE).find(script)?.groupValues?.get(1)
    val code = Regex("""^ *versionCode = ([0-9]+)$""", RegexOption.MULTILINE).find(script)?.groupValues?.get(1)
    return (name ?: "0.0.0") to (code?.toInt() ?: 0)
  }

  private companion object {
    const val DESKTOP_MAIN_CLASS = "com.hedvig.android.desktop.MainKt"

    /**
     * Key packages whose screens may write to the backend on entry, or whose first step does. The list is
     * conservative: a package is listed when its entry screen starts a flow (an intent, a quote, a conversation, a
     * login attempt, a payment setup), without proving each one writes.
     */
    val sessionModeExclusions: Map<String, String> = mapOf(
      "com.hedvig.android.feature.connect.payment.trustly" to "Trustly runs a payment setup mutation on entry",
      "com.hedvig.android.feature.claim.chat" to "claim chat starts a claim on entry",
      "com.hedvig.android.feature.terminateinsurance" to "termination starts a termination flow on entry",
      "com.hedvig.android.feature.movingflow" to "the moving flow creates a move intent on entry",
      "com.hedvig.android.feature.change.tier" to "the tier flow creates a change intent on entry",
      "com.hedvig.android.feature.addon.purchase" to "the addon flow requests quotes on entry",
      "com.hedvig.android.feature.remove.addons" to "the remove-addon flow starts on entry",
      "com.hedvig.android.feature.editcoinsured" to "co-insured editing is a write flow",
      "com.hedvig.android.feature.chat.navigation.ChatKey" to "a chat key may create a conversation",
      "com.hedvig.android.feature.login" to "login screens start a login attempt",
      "com.hedvig.android.feature.payin.account.navigation.SetupSwishPayinKey" to "Swish setup starts a payment request",
      "com.hedvig.android.feature.payoutaccount.navigation.SetupSwishPayoutKey" to "Swish setup starts a payout setup",
      "com.hedvig.android.feature.deleteaccount" to "account deletion is a write flow",
    )
  }
}
