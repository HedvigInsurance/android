package com.hedvig.android.desktop.walkthrough

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.semantics.Role
import androidx.navigation3.runtime.EntryProviderScope
import com.apollographql.apollo.api.Mutation
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.desktop.DesktopAppBuildConfig
import com.hedvig.android.desktop.DesktopBackend
import com.hedvig.android.desktop.DesktopGraph
import com.hedvig.android.desktop.installDesktopLogging
import com.hedvig.android.desktop.newBackstackController
import com.hedvig.android.desktop.registerSharedEntries
import com.hedvig.android.desktop.registeredKeyClasses
import com.hedvig.android.feature.chat.navigation.InboxKey
import com.hedvig.android.feature.claimhistory.nav.ClaimHistoryKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import dev.zacsweers.metro.createGraphFactory
import java.io.File
import java.util.Collections
import java.util.Locale
import java.util.TimeZone
import java.util.zip.ZipFile
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
 *   copied to a temporary directory and only the copy is used, unless `-Phedvig.walkthrough.sessionInPlace=true`,
 *   which uses the directory itself, so a token refresh is stored where the desktop app reads it.
 * - `-Phedvig.walkthrough.startOnly=true`: stop after the first logged-in screen, to check a session works.
 * - `-Phedvig.walkthrough.only=<text>`: only visit keys whose name contains the text.
 *
 * Demo mode turns on the app's demo mode, which is what the login screen's demo option does, so no account is used.
 * Screens without demo data call the staging backend unauthenticated and show its errors.
 *
 * Session mode shows real staging data. It only pushes keys and clicks the navigation bar, and never pushes a key
 * whose screen writes to the backend on entry (see [sessionModeExclusions]). It refuses to start while a desktop app
 * process runs, since both would refresh the same session. Run on a copy, a token refresh rotates the refresh token
 * and leaves the original directory with a dead one, so the desktop app has to log in again; run in place, it does
 * not. Keys that need an id get the first contract, claim or conversation id in the app's Apollo cache, after Home,
 * Insurances, the claim history and the inbox have loaded. After the keys, [investigate] runs.
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
    println("Mutation operation names known: ${mutationNames.size}")
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
      if (!onEdt { backstackController.isLoggedIn }) {
        println("The app did not reach a logged-in screen. Stopping before tabs and keys.")
        stoppedReason = "the app did not reach a logged-in screen (expired or missing session)"
        return
      }
      if (System.getProperty("hedvig.walkthrough.startOnly") == "true") {
        stoppedReason = "start only"
        return
      }
      visitTabs()
      val ids = if (mode == "demo") demoIds else sessionIds()
      visitKeys(mode, ids)
      investigate()
    } finally {
      closeScene()
      val header = listOf(
        "Mode: $mode ($demoEntryNote)",
        "Stopped early: ${stoppedReason ?: "no"}",
        "Ids: $idsNote",
        "Scene: ${spec.widthDp} x ${spec.heightDp} dp at density ${spec.density}, locale en-SE, zone Europe/Stockholm",
        "Settle: idle after ${spec.quietMillis} ms without invalidation, or settled after ${spec.settledMillis} ms " +
          "of unchanged semantics with no spinner, timeout ${spec.timeoutMillis} ms",
        "Notices shown by the app: ${notices.distinct().ifEmpty { listOf("none") }.joinToString(" / ")}",
      )
      collectOperations("end")
      val sentMutations = operationLog.filter { (_, operation) -> operation in mutationNames }
      File(outputDirectory, "operations.txt").writeText(
        operationLog.joinToString("\n") { (label, operation) ->
          "$label | $operation${if (operation in mutationNames) " | MUTATION" else ""}"
        },
      )
      writeIndex(
        File(outputDirectory, "index.md"),
        header + (
          "GraphQL operations: ${operationLog.size} (operations.txt), mutations among them: " +
            if (sentMutations.isEmpty()) {
              "none"
            } else {
              sentMutations.joinToString { (label, operation) ->
                "$operation ($label)"
              }
            }
        ),
        visits,
      )
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

  private var stoppedReason: String? = null

  /** Every GraphQL operation the app started, with the visit it belongs to, in order. */
  private val operationLog = mutableListOf<Pair<String, String>>()

  /** The operation names of every generated GraphQL mutation on the classpath. */
  private val mutationNames: Set<String> by lazy {
    System.getProperty("java.class.path").split(File.pathSeparator).map(::File).flatMap { entry ->
      when {
        entry.isDirectory -> entry.walkTopDown().filter {
          it.isFile
        }.map { it.relativeTo(entry).invariantSeparatorsPath }
          .toList()

        entry.isFile && entry.name.endsWith(".jar") -> ZipFile(entry).use { zip ->
          zip.entries().toList().map { it.name }
        }

        else -> emptyList()
      }
    }
      .filter { path -> path.startsWith("octopus/") && path.endsWith("Mutation.class") }
      .map { path -> path.removeSuffix(".class").replace('/', '.') }
      .filter { name ->
        Mutation::class.java.isAssignableFrom(Class.forName(name, false, Mutation::class.java.classLoader))
      }
      .map { name -> name.substringAfterLast('.').removeSuffix("Mutation") }
      .toSet()
  }

  private fun collectOperations(label: String): List<String> =
    logger.drainOperations().also { operations -> operationLog += operations.map { label to it } }

  private fun drainBetweenVisits() {
    logger.drain()
    collectOperations("between visits")
  }

  private var idsNote: String = "demo ids"

  /**
   * Ids for keys that need one, taken from what the app has loaded: contracts from Home and Insurances, and claims
   * and conversations after the read-only claim history and inbox screens have been shown once.
   */
  private fun sessionIds(): Map<String, String> {
    for (key in listOf<HedvigNavKey>(InboxKey, ClaimHistoryKey)) {
      onEdt { Snapshot.withMutableSnapshot { backstackController.entries.add(key) } }
      scene!!.settle()
      onEdt {
        Snapshot.withMutableSnapshot { backstackController.entries.removeAt(backstackController.entries.lastIndex) }
      }
      scene!!.settle(minimumMillis = 0)
    }
    drainBetweenVisits()
    val loaded = LoadedData.read(graph.apolloClient)
    val contracts = loaded.idsOf("Contract")
    val claims = loaded.idsOf("Claim")
    val conversations = loaded.idsOf("Conversation")
    idsNote = "from the Apollo cache: ${contracts.size} contracts, ${claims.size} claims, " +
      "${conversations.size} conversations; the first of each is used"
    return buildMap {
      contracts.firstOrNull()?.let {
        put("contractId", it)
        put("insuranceId", it)
      }
      claims.firstOrNull()?.let { put("claimId", it) }
      conversations.firstOrNull()?.let { put("conversationId", it) }
    }
  }

  /**
   * Looks at three behaviours reported from the real window, each in a fresh scene from the Home tab:
   * - the navigation bar after clicking each tab in turn, after the pointer leaves the bar, after only hovering a
   *   tab, and after switching tabs through the back stack with no pointer event at all;
   * - scrolling Home down with the mouse wheel until the lower section reaches the top, then back up;
   * - every markdown link the app has cached, to find where the help center's "Go to payments" link points.
   */
  private fun investigate() {
    fun freshHome() {
      openScene()
      onEdt { backstackController.selectTopLevel(TopLevelTab.Home) }
      scene!!.settle()
      drainBetweenVisits()
      uncaught.clear()
    }

    fun tabs(): List<VisibleNode> = scene!!.visibleNodes().filter { it.role == Role.Tab }.sortedBy { it.bounds.left }

    freshHome()
    for (index in tabs().indices) {
      val tab = tabs()[index]
      scene!!.click(tab.bounds)
      record(
        "inv-navbar-click-${index + 1}-${tab.text}",
        "investigation",
        scene!!.settle(),
        listOf("clicked ${tab.text}"),
      )
    }
    scene!!.movePointer(androidx.compose.ui.geometry.Offset(412f, 600f))
    record("inv-navbar-pointer-left-bar", "investigation", scene!!.settle(), listOf("pointer moved to the content"))

    freshHome()
    tabs().getOrNull(2)?.let { tab ->
      scene!!.movePointer(tab.bounds.center)
      record("inv-navbar-hover-only", "investigation", scene!!.settle(), listOf("pointer over ${tab.text}, no press"))
      scene!!.movePointer(androidx.compose.ui.geometry.Offset(412f, 600f))
      record("inv-navbar-hover-left", "investigation", scene!!.settle(), listOf("pointer moved away from ${tab.text}"))
    }

    freshHome()
    for (tab in TopLevelTab.entries) {
      onEdt { backstackController.selectTopLevel(tab) }
      record(
        "inv-navbar-backstack-${tab.name}",
        "investigation",
        scene!!.settle(),
        listOf("selectTopLevel($tab), no pointer"),
      )
    }

    freshHome()
    val middle = androidx.compose.ui.geometry.Offset(412f, 1200f)
    record("inv-home-scroll-0-before", "investigation", scene!!.settle(), listOf("Home before scrolling"))
    scene!!.wheel(middle, deltaY = 3f, count = 15)
    record(
      "inv-home-scroll-1-down",
      "investigation",
      scene!!.settle(),
      listOf("15 wheel events of +3 at (412, 1200) px"),
    )
    scene!!.wheel(middle, deltaY = -3f, count = 15)
    record("inv-home-scroll-2-up", "investigation", scene!!.settle(), listOf("15 wheel events of -3 at (412, 1200) px"))
    val top = androidx.compose.ui.geometry.Offset(412f, 300f)
    scene!!.wheel(top, deltaY = -3f, count = 15)
    record(
      "inv-home-scroll-3-up-higher",
      "investigation",
      scene!!.settle(),
      listOf("15 wheel events of -3 at (412, 300) px"),
    )

    val links = LoadedData.read(graph.apolloClient).markdownLinks()
    File(outputDirectory, "cached-markdown-links.txt").writeText(
      links.joinToString("\n") { (field, text, url) -> "$field | $text | $url" },
    )
    println("Cached markdown links: ${links.size}")
  }

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
    drainBetweenVisits()
    uncaught.clear()
    val scene = scene ?: error("No scene")
    val settle = scene.settle()
    return record(name, kind, settle, notes)
  }

  private fun record(name: String, kind: String, settle: SettleResult, notes: List<String>): Visit {
    val scene = scene ?: error("No scene")
    val problems = logger.drain()
    val operations = collectOperations(name)
    val mutations = operations.filter { it in mutationNames }
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
      if (mutations.isNotEmpty()) add("MUTATIONS SENT: ${mutations.joinToString()}")
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
      drainBetweenVisits()
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

  private fun visitKeys(mode: String, ids: Map<String, String>) {
    val only = System.getProperty("hedvig.walkthrough.only")
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
          drainBetweenVisits()
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
          drainBetweenVisits()
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
    if (System.getProperty("hedvig.walkthrough.sessionInPlace") == "true") return source
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
     * Keys whose screen, or a screen it forwards to by itself, writes to the backend while loading, with the write.
     * Each was found by reading the entry's ViewModel or presenter. A prefix excludes every key below it.
     */
    val sessionModeExclusions: Map<String, String> = mapOf(
      "com.hedvig.android.feature.connect.payment.trustly" to
        "SetupTrustlyPayoutMutation (StartTrustlySessionUseCase, TrustlyPresenter load)",
      "com.hedvig.android.feature.claim.chat.navigation.ClaimChatKey" to
        "ClaimIntentStartMutation (StartClaimIntentUseCase, ClaimChatViewModel load)",
      "com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey" to
        "ClaimAcknowledgeClosedStatusMutation (GetClaimDetailUiStateUseCase load, for a closed claim)",
      "com.hedvig.android.feature.movingflow" to
        "MoveIntentV2CreateMutation (SelectContractViewModel load); the later steps need its move intent id",
      "com.hedvig.android.feature.change.tier" to
        "ChangeTierDeductibleCreateIntentMutation (StartTierFlowViewModel load; ChooseInsuranceViewModel load " +
        "when one insurance is eligible); the later steps need its intent",
      "com.hedvig.android.feature.addon.purchase" to
        "AddonGenerateOfferMutation (CustomizeAddonViewModel load, which the addon entries forward to by " +
        "themselves for a single insurance)",
      "com.hedvig.android.feature.help.center.navigation.PuppyGuideArticleKey" to
        "PuppyGuideEngagementMutation (PuppyArticleViewModel, as soon as the article is scrolled to the end, " +
        "which a short article is on load)",
      "com.hedvig.android.feature.payin.account.navigation.SetupSwishPayinKey" to
        "SetupSwishPayinMutation (SwishPayinStatusViewModel load)",
      "com.hedvig.android.feature.login.navigation.SwedishLoginKey" to
        "starts a BankID login attempt on the auth service on load",
    )
  }
}
