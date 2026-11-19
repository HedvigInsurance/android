package com.hedvig.android.umbrella

import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.datastore.DeviceIdFetcher
import com.hedvig.android.iosdata.bridge.IosGraphHolder
import com.hedvig.android.language.IosLanguageSource
import com.hedvig.android.language.Language
import com.hedvig.android.logger.IosLogSink
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.network.clients.AccessTokenFetcher
import com.hedvig.android.network.clients.NetworkLogPolicy
import kotlin.concurrent.AtomicInt
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import platform.Foundation.NSDate
import platform.Foundation.NSLock
import platform.Foundation.NSRunLoop
import platform.Foundation.NSThread
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate

/** The iOS app's inputs as Kotlin fakes, each recording how often, and on which threads, Kotlin calls it. */
internal class FakeInputs(
  val appFlavor: Flavor = Flavor.Staging,
  val accessTokenFetcher: FakeAccessTokenFetcher = FakeAccessTokenFetcher(),
  val deviceIdFetcher: FakeDeviceIdFetcher = FakeDeviceIdFetcher(),
  val languageSource: FakeLanguageSource = FakeLanguageSource(),
  val networkLogPolicy: NetworkLogPolicy = NetworkLogPolicy(
    logsHttpBodies = false,
    logsGraphqlData = false,
    logsGraphqlErrors = false,
  ),
) {
  /** The real graph, as the iOS app creates it. Its engine is Darwin, so no test lets it send a request. */
  fun realHolder(): IosGraphHolder = createIosGraphHolder(
    appFlavor = appFlavor,
    accessTokenFetcher = accessTokenFetcher,
    deviceIdFetcher = deviceIdFetcher,
    languageSource = languageSource,
    networkLogPolicy = networkLogPolicy,
  )

  /** How many times Kotlin called any input. */
  fun inputCalls(): Int = accessTokenFetcher.calls.value + deviceIdFetcher.calls.value + languageSource.calls.value
}

/** Which threads an input was called on. */
internal open class ThreadRecorder {
  private val lock = NSLock()
  private val mainThreadCalls = mutableListOf<Boolean>()
  val calls = AtomicInt(0)

  protected fun record() {
    calls.incrementAndGet()
    lock.lock()
    try {
      mainThreadCalls += NSThread.isMainThread
    } finally {
      lock.unlock()
    }
  }

  fun calledOnMainThread(): List<Boolean> {
    lock.lock()
    try {
      return mainThreadCalls.toList()
    } finally {
      lock.unlock()
    }
  }
}

internal class FakeAccessTokenFetcher(
  private val throws: Boolean = false,
  private val neverAnswers: Boolean = false,
) : ThreadRecorder(), AccessTokenFetcher {
  override suspend fun fetch(): String? {
    record()
    if (throws) throw IllegalStateException("token refresh failed")
    // A Swift token fetcher that never calls back: no resume, and Kotlin's cancel does not reach it.
    if (neverAnswers) suspendCoroutine<Nothing> { }
    return ACCESS_TOKEN
  }
}

internal class FakeDeviceIdFetcher : ThreadRecorder(), DeviceIdFetcher {
  override suspend fun fetch(): String? {
    record()
    return DEVICE_ID
  }
}

internal class FakeLanguageSource(private val language: Language = Language.EN_SE) :
  ThreadRecorder(),
  IosLanguageSource {
  override fun currentLanguage(): Language {
    record()
    return language
  }
}

/** The iOS app's log sink as a test sees it: every line, from any thread. */
internal class RecordingSink : IosLogSink {
  private val lock = NSLock()
  private val lines = mutableListOf<Pair<LogPriority, String>>()

  fun entries(): List<Pair<LogPriority, String>> {
    lock.lock()
    try {
      return lines.toList()
    } finally {
      lock.unlock()
    }
  }

  fun messages(): List<String> = entries().map { it.second }

  override fun log(priority: LogPriority, tag: String?, message: String) {
    lock.lock()
    try {
      lines += priority to message
    } finally {
      lock.unlock()
    }
  }
}

/** Drives the main run loop in 10 ms slices until [condition] holds or [timeout] passes. */
internal fun pumpMainLoopUntil(timeout: Duration = 10.seconds, condition: () -> Boolean): Boolean {
  val start = TimeSource.Monotonic.markNow()
  while (!condition()) {
    if (start.elapsedNow() > timeout) return false
    NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.01))
  }
  return true
}

internal const val ACCESS_TOKEN = "test-access-token-5d1c"
internal const val DEVICE_ID = "test-device-id-0b7e"
