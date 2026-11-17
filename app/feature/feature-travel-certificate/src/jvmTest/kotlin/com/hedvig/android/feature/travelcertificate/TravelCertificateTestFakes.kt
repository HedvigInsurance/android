package com.hedvig.android.feature.travelcertificate

import androidx.compose.runtime.mutableStateListOf
import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.network.NetworkTransport
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
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
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import octopus.TravelCertificateCreateMutation
import octopus.builder.Data
import octopus.builder.buildTravelCertificate
import octopus.type.TravelCertificateCreateInput

/**
 * Asserts that the latest state emitted since the last read, if any, equals [expected]. Recompositions re-emit an
 * unchanged state, so `expectNoEvents` cannot express "nothing changed".
 */
internal fun <State> MoleculePresenterTestContext<*, State>.expectNoStateChange(expected: State) {
  val latest = try {
    expectMostRecentItem()
  } catch (_: AssertionError) {
    return
  }
  assertThat(latest).isEqualTo(expected)
}

internal class TestBackstack(vararg initialEntries: HedvigNavKey) : Backstack {
  override val entries: MutableList<HedvigNavKey> = mutableStateListOf(*initialEntries)
}

/** Noon UTC, so "today" is the same calendar day in every zone the tests may run in. */
internal class FixedClock(private val now: Instant = Instant.parse("2026-11-17T12:00:00Z")) : Clock {
  override fun now(): Instant = now
}

internal class FakeGetTravelCertificatesHistoryUseCase : GetTravelCertificatesHistoryUseCase {
  val responses = Channel<Either<ErrorMessage, List<TravelCertificate>>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, List<TravelCertificate>> {
    calls++
    return responses.receive()
  }
}

internal class FakeCheckAvailabilityUseCase : CheckTravelCertificateAvailabilityForCurrentContractsUseCase {
  val responses = Channel<Either<ErrorMessage, Boolean>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, Boolean> {
    calls++
    return responses.receive()
  }
}

internal class FakeGetEligibleContractsWithAddressUseCase : GetEligibleContractsWithAddressUseCase {
  val responses = Channel<Either<ErrorMessage, List<ContractEligibleWithAddress>>>(Channel.UNLIMITED)
  var calls = 0

  override suspend fun invoke(): Either<ErrorMessage, List<ContractEligibleWithAddress>> {
    calls++
    return responses.receive()
  }
}

internal class FakeGetAddonBannerInfoUseCase : GetAddonBannerInfoUseCase {
  val banners = MutableSharedFlow<Either<ErrorMessage, List<AddonBannerInfo>>>(replay = 1)
  val sources = mutableListOf<AddonBannerSource>()

  override fun invoke(source: AddonBannerSource): Flow<Either<ErrorMessage, List<AddonBannerInfo>>> {
    sources += source
    return banners
  }
}

internal class FakeDownloadPdfUseCase : DownloadPdfUseCase {
  val responses = Channel<Either<ErrorMessage, DownloadedFile>>(Channel.UNLIMITED)
  val urls = mutableListOf<String>()

  override suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile> {
    urls += url
    return responses.receive()
  }
}

internal class FakeGetTravelCertificateSpecificationsUseCase : GetTravelCertificateSpecificationsUseCase {
  val responses = Channel<Either<TravelCertificateError, TravelCertificateData>>(Channel.UNLIMITED)
  val contractIds = mutableListOf<String?>()

  override suspend fun invoke(contractId: String?): Either<TravelCertificateError, TravelCertificateData> {
    contractIds += contractId
    return responses.receive()
  }
}

internal class FakeGetCoInsuredForContractUseCase : GetCoInsuredForContractUseCase {
  val responses = MutableSharedFlow<Either<ErrorMessage, CoInsuredDataWithMember>>(replay = 1)
  val contractIds = mutableListOf<String>()

  override fun invoke(contractId: String): Flow<Either<ErrorMessage, CoInsuredDataWithMember>> {
    contractIds += contractId
    return responses
  }
}

/**
 * Answers [TravelCertificateCreateMutation] in place of the network, so the real [CreateTravelCertificateUseCase]
 * runs on top of it. Each request waits for one entry in [responses]: a signed url, or null for a network failure.
 * The client runs on [Dispatchers.Unconfined] so requests stay on the test scheduler.
 */
internal class FakeCreateTravelCertificateTransport : NetworkTransport {
  val responses = Channel<String?>(Channel.UNLIMITED)
  val inputs = mutableListOf<TravelCertificateCreateInput>()

  override fun <D : Operation.Data> execute(request: ApolloRequest<D>): Flow<ApolloResponse<D>> = flow {
    val operation = request.operation as TravelCertificateCreateMutation
    inputs += operation.input
    val signedUrl = responses.receive()
    val builder = ApolloResponse.Builder(request.operation, request.requestUuid)
    val response = if (signedUrl == null) {
      builder.exception(ApolloNetworkException("Fake network failure"))
    } else {
      val data = TravelCertificateCreateMutation.Data(OctopusFakeResolver) {
        travelCertificateCreate = buildTravelCertificate { this.signedUrl = signedUrl }
      }
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
