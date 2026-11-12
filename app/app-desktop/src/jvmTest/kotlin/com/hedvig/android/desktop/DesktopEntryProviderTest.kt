package com.hedvig.android.desktop

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isTrue
import assertk.assertions.startsWith
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.chat.navigation.ChatKey
import com.hedvig.android.feature.chat.navigation.InboxKey
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.imageviewer.navigation.ImageViewerKey
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceEvidenceKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.feature.onboarding.navigation.OnboardingKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import io.github.classgraph.ClassGraph
import java.io.File
import java.lang.reflect.InvocationTargetException
import kotlin.io.path.createTempDirectory
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Rule
import org.junit.Test

class DesktopEntryProviderTest {
  private val recordingLogger = RecordingLogcatLogger()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule(recordingLogger)

  private val dataDirectory: File = createTempDirectory("desktop-entry-provider-test").toFile()

  private val backstack = newBackstackController()

  @After
  fun tearDown() {
    dataDirectory.deleteRecursively()
  }

  /**
   * Every concrete `@Serializable` [HedvigNavKey] on the desktop classpath is a key the back stack can hold, so the
   * shared provider must register an entry for its class, and the desktop fallback is never needed for it. The
   * registered classes are read from the [EntryProviderScope] after registration, since most keys are `internal` to
   * their feature and cannot be constructed here.
   */
  @Test
  fun `the shared provider registers an entry for every navigation key on the classpath`() {
    val scope = EntryProviderScope<HedvigNavKey> { key -> error("No entry is registered for $key") }
    registerSharedEntries(scope, createTestDesktopGraph(dataDirectory))
    val registeredKeyClasses = scope.registeredKeyClasses()
    val navigationKeyClasses = concreteSerializableNavKeysOnClasspath()

    assertThat(navigationKeyClasses).containsAtLeast(ChatKey::class, InboxKey::class, HomeKey::class, LoginKey::class)
    assertThat((navigationKeyClasses - registeredKeyClasses).map { it.qualifiedName }).isEmpty()
  }

  /**
   * Resolves keys through a provider built the way the running desktop app builds it, but with Navigation 3's
   * default fallback, which throws. Navigation 3 also throws on a second registration of a key class.
   */
  @Test
  fun `the keys the app opens resolve through the shared provider without a fallback`() {
    val provider = entryProvider<HedvigNavKey> {
      registerSharedEntries(this, createTestDesktopGraph(dataDirectory))
    }

    publicKeys.forEach { key ->
      assertThat(provider(key).contentKey.toString()).startsWith(key.toString())
    }
  }

  @Test
  fun `the shared tab roots keep the navigation bar`() {
    val provider = entryProvider<HedvigNavKey> {
      registerSharedEntries(this, createTestDesktopGraph(dataDirectory))
    }
    val showNavBar = NavSuiteSceneDecoratorStrategy.showNavBar()

    listOf(HomeKey, InsurancesKey).forEach { key ->
      assertThat(provider(key).metadata.entries.containsAll(showNavBar.entries)).isTrue()
    }
  }

  @Test
  fun `a key without an entry is rendered by the fallback and logged once`() {
    val provider = entryProvider(fallback = desktopFallbackEntry(backstack)) {}

    repeat(3) { assertThat(provider(UnregisteredKey).contentKey.toString()).startsWith(UnregisteredKey.toString()) }

    assertThat(recordingLogger.lines.filter { it.first == LogPriority.WARN }).containsExactly(
      LogPriority.WARN to "No entry is registered for UnregisteredKey, showing it as not available on desktop",
    )
  }

  private companion object {
    /** Keys this test can construct, one or more per feature the desktop app opens. */
    val publicKeys = listOf(
      LoginKey,
      HomeKey,
      InsurancesKey,
      OnboardingKey,
      TravelCertificateKey,
      InsuranceEvidenceKey,
      ClaimDetailsKey("claim-id"),
      ImageViewerKey("https://example.com/image.png", "cache-key"),
      TrustlyKey(),
      ChatKey("conversation-id"),
      InboxKey,
    )
  }
}

/** The key classes registered on this scope, read from Navigation 3's private registration maps. */
private fun EntryProviderScope<HedvigNavKey>.registeredKeyClasses(): Set<KClass<*>> {
  fun declaredMap(name: String): Map<*, *> = EntryProviderScope::class.java.getDeclaredField(name)
    .apply { isAccessible = true }
    .get(this) as Map<*, *>
  val byClass = declaredMap("clazzProviders").keys.map { it as KClass<*> }
  val byInstance = declaredMap("providers").keys.map { it!!::class }
  return (byClass + byInstance).toSet()
}

private fun concreteSerializableNavKeysOnClasspath(): Set<KClass<*>> = ClassGraph()
  .enableClasspath()
  .enableClassInfo()
  .enableAnnotationInfo()
  .acceptPackages("com.hedvig")
  .scan()
  .use { scan ->
    scan.getAllClassesImplementing(HedvigNavKey::class.java.name)
      .filter { !it.isInterface && !it.isAbstract }
      .filter { it.hasAnnotation("kotlinx.serialization.Serializable") }
      .map { Class.forName(it.name, false, HedvigNavKey::class.java.classLoader).kotlin }
      .toSet()
  }

/**
 * Calls the shared `hedvigEntryProvider`, which is `internal` to `:app-shell`, through reflection, with the desktop
 * graph's dependencies. Its JVM signature is the Kotlin one with the receiver first, so a rename or a change to its
 * parameters fails here with a message rather than silently.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun registerSharedEntries(scope: EntryProviderScope<HedvigNavKey>, graph: DesktopGraph) {
  val method = Class.forName("com.hedvig.android.app.navigation.HedvigEntryProviderKt")
    .declaredMethods
    .singleOrNull { it.name == "hedvigEntryProvider" }
    ?: error("HedvigEntryProviderKt has no single hedvigEntryProvider function, update this call")
  method.isAccessible = true
  val arguments = arrayOf<Any>(
    scope,
    newBackstackController(),
    CoroutineScope(Job()),
    WindowSizeClass.calculateFromSize(DpSize(412.dp, 915.dp)),
    graph.memberIdService,
    GlobalSnackBarState(),
    DesktopExternalNavigator(showNotice = {}),
    DesktopAppHost(showNotice = {}),
    { _: String -> },
    { _: String -> true },
    graph.imageLoader,
    graph.languageService,
    graph.hedvigBuildConstants,
    graph.resetOnboardingSeenUseCase,
  )
  check(method.parameterTypes.size == arguments.size) {
    "hedvigEntryProvider takes ${method.parameterTypes.toList()}, update this call"
  }
  try {
    method.invoke(null, *arguments)
  } catch (e: InvocationTargetException) {
    throw e.targetException
  }
}

private data object UnregisteredKey : HedvigNavKey
