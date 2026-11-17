package com.hedvig.android.feature.travelcertificate.ios

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.network.NetworkTransport
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.core.fileupload.DownloadedFile
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.feature.travelcertificate.data.CheckTravelCertificateAvailabilityForCurrentContractsUseCase
import com.hedvig.android.feature.travelcertificate.data.CoInsuredDataWithMember
import com.hedvig.android.feature.travelcertificate.data.ContractEligibleWithAddress
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetCoInsuredForContractUseCase
import com.hedvig.android.feature.travelcertificate.data.GetEligibleContractsWithAddressUseCase
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificatesHistoryUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificate
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateData
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateError
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import kotlin.concurrent.AtomicInt
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.LocalDate
import octopus.TravelCertificateCreateMutation
import octopus.type.TravelCertificateCreateInput
import platform.Foundation.NSDate
import platform.Foundation.NSLock
import platform.Foundation.NSRunLoop
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate

/** Noon UTC, so "today" is the same calendar day in every zone the simulator may run in. */
internal class FixedClock(private val now: Instant = Instant.parse("2026-11-17T12:00:00Z")) : Clock {
  override fun now(): Instant = now
}

internal class FakeHistoryUseCase : GetTravelCertificatesHistoryUseCase {
  val calls = AtomicInt(0)

  override suspend fun invoke(): Either<ErrorMessage, List<TravelCertificate>> {
    calls.incrementAndGet()
    return Either.Left(ErrorMessage())
  }
}

internal class FakeAvailabilityUseCase : CheckTravelCertificateAvailabilityForCurrentContractsUseCase {
  val calls = AtomicInt(0)

  override suspend fun invoke(): Either<ErrorMessage, Boolean> {
    calls.incrementAndGet()
    return Either.Left(ErrorMessage())
  }
}

internal class FakeEligibleContractsUseCase : GetEligibleContractsWithAddressUseCase {
  val calls = AtomicInt(0)

  override suspend fun invoke(): Either<ErrorMessage, List<ContractEligibleWithAddress>> {
    calls.incrementAndGet()
    return Either.Left(ErrorMessage())
  }
}

internal class FakeAddonBannerUseCase : GetAddonBannerInfoUseCase {
  val calls = AtomicInt(0)

  override fun invoke(source: AddonBannerSource): Flow<Either<ErrorMessage, List<AddonBannerInfo>>> =
    flow<Either<ErrorMessage, List<AddonBannerInfo>>> { emit(Either.Right(emptyList())) }
      .onStart { calls.incrementAndGet() }
}

internal class FakeDownloadPdfUseCase : DownloadPdfUseCase {
  val calls = AtomicInt(0)

  override suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile> {
    calls.incrementAndGet()
    return Either.Left(ErrorMessage())
  }
}

/** Answers from [responses] in order; each call waits for one. */
internal class FakeSpecificationsUseCase : GetTravelCertificateSpecificationsUseCase {
  val responses = Channel<Either<TravelCertificateError, TravelCertificateData>>(Channel.UNLIMITED)
  private val lock = NSLock()
  private val received = mutableListOf<String?>()

  fun contractIds(): List<String?> = locked(lock) { received.toList() }

  override suspend fun invoke(contractId: String?): Either<TravelCertificateError, TravelCertificateData> {
    locked(lock) { received += contractId }
    return responses.receive()
  }
}

internal class FakeCoInsuredUseCase : GetCoInsuredForContractUseCase {
  val responses = MutableSharedFlow<Either<ErrorMessage, CoInsuredDataWithMember>>(replay = 1)
  private val lock = NSLock()
  private val received = mutableListOf<String>()

  fun contractIds(): List<String> = locked(lock) { received.toList() }

  override fun invoke(contractId: String): Flow<Either<ErrorMessage, CoInsuredDataWithMember>> {
    locked(lock) { received += contractId }
    return responses
  }
}

/**
 * Answers the create mutation in place of the network, so the real [CreateTravelCertificateUseCase] runs on top of
 * it. Each request waits for one entry in [responses]: a signed url, or null for a network failure.
 */
internal class FakeCreateTransport : NetworkTransport {
  val responses = Channel<String?>(Channel.UNLIMITED)
  private val lock = NSLock()
  private val received = mutableListOf<TravelCertificateCreateInput>()

  fun inputs(): List<TravelCertificateCreateInput> = locked(lock) { received.toList() }

  override fun <D : Operation.Data> execute(request: ApolloRequest<D>): Flow<ApolloResponse<D>> = flow {
    val operation = request.operation as TravelCertificateCreateMutation
    locked(lock) { received += operation.input }
    val signedUrl = responses.receive()
    val builder = ApolloResponse.Builder(request.operation, request.requestUuid)
    val response = if (signedUrl == null) {
      builder.exception(ApolloNetworkException("Fake network failure"))
    } else {
      val data = TravelCertificateCreateMutation.Data(
        TravelCertificateCreateMutation.Data.TravelCertificateCreate(
          __typename = "TravelCertificate",
          id = "certificate-1",
          signedUrl = signedUrl,
          startDate = LocalDate(2026, 12, 1),
          endDate = null,
        ),
      )
      @Suppress("UNCHECKED_CAST")
      builder.data(data as D)
    }
    emit(response.isLast(true).build())
  }

  override fun dispose() {}

  fun createUseCase(): CreateTravelCertificateUseCase = CreateTravelCertificateUseCase(
    ApolloClient.Builder()
      .networkTransport(this)
      .dispatcher(Dispatchers.Unconfined)
      .build(),
  )
}

internal fun pumpMainLoop(duration: Duration) {
  pumpMainLoopUntil(duration) { false }
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

/** Logs arrive from the main thread and from other threads. */
internal class CapturingLogger : LogcatLogger {
  private val lock = NSLock()
  private val captured = mutableListOf<Pair<LogPriority, String>>()

  fun entries(): List<Pair<LogPriority, String>> = locked(lock) { captured.toList() }

  fun messages(): List<String> = entries().map { it.second }

  /** The events [viewModelName] dropped, as the overflow error each drop logs prints them. */
  fun droppedEvents(viewModelName: String): List<String> = entries()
    .filter { (priority, message) -> priority == LogPriority.WARN && message.startsWith("$viewModelName dropped") }
    .map { (_, message) -> message.substringAfter("Event buffer overflow on event:").substringBefore("\n") }
    .map { it.removeSuffix(".") }

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    val text = message() + (throwable?.let { "\n" + it.stackTraceToString() } ?: "")
    locked(lock) { captured += priority to text }
  }
}

private fun <T> locked(lock: NSLock, block: () -> T): T {
  lock.lock()
  try {
    return block()
  } finally {
    lock.unlock()
  }
}
