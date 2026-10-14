package com.hedvig.android.feature.inapppurchase.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.delay

internal interface GetInAppPurchaseSigningUseCase {
  suspend fun invoke(signingId: String): Either<ErrorMessage, InAppPurchaseSigning>
}

// `shopSessionSigning` already exists in the schema; it is only commented out here because the signing ids it
// would be queried with come from the mocked sign mutation.
//
// query InAppPurchaseSigning($signingId: UUID!) {
//   shopSessionSigning(id: $signingId) {
//     ...InAppPurchaseSigningFragment
//   }
// }
//
// fragment InAppPurchaseSigningFragment on ShopSessionSigning {
//   id
//   status
//   seBankidProperties {
//     autoStartToken
//     liveQrCodeData
//     bankidAppOpened
//   }
//   userError { message }
// }
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetInAppPurchaseSigningUseCaseImpl(
  @Suppress("unused") private val apolloClient: ApolloClient,
) : GetInAppPurchaseSigningUseCase {
  override suspend fun invoke(signingId: String): Either<ErrorMessage, InAppPurchaseSigning> {
    return either {
      // apolloClient
      //   .query(InAppPurchaseSigningQuery(signingId))
      //   .fetchPolicy(FetchPolicy.NetworkOnly)
      //   .safeExecute()
      //   .onLeft { logcat(LogPriority.ERROR, it) { "InAppPurchaseSigningQuery for $signingId failed" } }
      //   .mapLeft { ErrorMessage() }
      //   .bind()
      //   .shopSessionSigning
      //   .toInAppPurchaseSigning()
      delay(MockNetworkDelay)
      InAppPurchaseMocks.nextSigningPoll()
    }
  }
}

// internal fun InAppPurchaseSigningFragment.toInAppPurchaseSigning() = InAppPurchaseSigning(
//   id = id,
//   status = when (status) {
//     ShopSessionSigningStatus.PENDING -> InAppPurchaseSigningStatus.PENDING
//     ShopSessionSigningStatus.CREATING -> InAppPurchaseSigningStatus.CREATING
//     ShopSessionSigningStatus.SIGNED -> InAppPurchaseSigningStatus.SIGNED
//     ShopSessionSigningStatus.FAILED,
//     ShopSessionSigningStatus.UNKNOWN__,
//     -> InAppPurchaseSigningStatus.FAILED
//   },
//   autoStartToken = seBankidProperties?.autoStartToken,
//   liveQrCodeData = seBankidProperties?.liveQrCodeData,
//   bankIdAppOpened = seBankidProperties?.bankidAppOpened == true,
//   failureMessage = userError?.message,
// )
