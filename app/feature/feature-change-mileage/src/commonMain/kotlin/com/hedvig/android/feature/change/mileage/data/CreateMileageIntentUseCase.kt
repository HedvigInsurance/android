package com.hedvig.android.feature.change.mileage.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal interface CreateMileageIntentUseCase {
  suspend fun invoke(contractId: String): Either<CreateMileageIntentError, ChangeMileageIntent>
}

// TODO(change-mileage): once `changeMileageCreateIntent` is published, uncomment
//  MutationChangeMileageCreateIntent.graphql and this class, and move the @ContributesBinding here from
//  FakeCreateMileageIntentUseCase, deleting the fake.
//
// @ContributesBinding(AppScope::class)
// @Inject
// internal class CreateMileageIntentUseCaseImpl(
//   private val apolloClient: ApolloClient,
// ) : CreateMileageIntentUseCase {
//   override suspend fun invoke(contractId: String): Either<CreateMileageIntentError, ChangeMileageIntent> = either {
//     val output = apolloClient
//       .mutation(ChangeMileageCreateIntentMutation(contractId))
//       .safeExecute()
//       .onLeft { logcat(LogPriority.WARN, it) { "Failed to create change mileage intent for $contractId" } }
//       .mapLeft { CreateMileageIntentError.General }
//       .bind()
//       .changeMileageCreateIntent
//     when (output) {
//       is ChangeMileageCreateIntentMutation.Data.ChangeMileageCreateIntent.UserErrorChangeMileageCreateIntent -> {
//         raise(CreateMileageIntentError.UserError(output.message))
//       }
//
//       is ChangeMileageCreateIntentMutation.Data.ChangeMileageCreateIntent.ChangeMileageIntentChangeMileageCreateIntent -> {
//         ChangeMileageIntent(
//           activationDate = output.activationDate,
//           currentTotalCost = ItemCost.fromItemCostFragment(output.currentTotalCost),
//           quotes = output.quotes.map { quote ->
//             MileageQuote(
//               id = quote.id,
//               displayName = quote.displayName,
//               isCurrent = quote.isCurrent,
//               totalCost = ItemCost.fromItemCostFragment(quote.totalCost),
//               infoMessage = quote.infoMessage,
//               displayItems = quote.displayItems.map {
//                 MileageDisplayItem(it.displayTitle, it.displayValue, it.previousDisplayValue)
//               },
//             )
//           },
//         )
//       }
//
//       else -> {
//         logcat(LogPriority.ERROR) { "Unknown changeMileageCreateIntent output: ${output.__typename}" }
//         raise(CreateMileageIntentError.General)
//       }
//     }
//   }
// }

/**
 * Stands in for the backend until `changeMileageCreateIntent` is published. Returns the five mileage bands from the
 * design with 1 500 mil as the current one.
 */
@ContributesBinding(AppScope::class)
@Inject
internal class FakeCreateMileageIntentUseCase(
  private val clock: Clock,
) : CreateMileageIntentUseCase {
  override suspend fun invoke(contractId: String): Either<CreateMileageIntentError, ChangeMileageIntent> {
    delay(1.seconds)
    val bands = listOf(
      "1 000 mil" to 371.0,
      "1 500 mil" to 412.0,
      "2 000 mil" to 449.0,
      "2 500 mil" to 486.0,
      "Över 2 500 mil" to 529.0,
    )
    val currentIndex = 1
    val currentBand = bands[currentIndex].first
    val quotes = bands.mapIndexed { index, (displayName, price) ->
      val isCurrent = index == currentIndex
      MileageQuote(
        id = "fake-mileage-quote-$index",
        displayName = displayName,
        isCurrent = isCurrent,
        totalCost = fakeItemCost(price),
        infoMessage = if (index < currentIndex) {
          "Om bilen körs längre än du har valt kan ersättningen bli lägre vid en skada. " +
            "Du kan ändra körsträckan när du vill."
        } else {
          null
        },
        displayItems = listOf(
          MileageDisplayItem(
            title = "Körsträcka per år",
            value = displayName,
            previousValue = if (isCurrent) null else currentBand,
          ),
          MileageDisplayItem(title = "Registreringsnummer", value = "ABC 123", previousValue = null),
          MileageDisplayItem(title = "Skyddsnivå", value = "Helförsäkring", previousValue = null),
        ),
      )
    }
    return ChangeMileageIntent(
      activationDate = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
      currentTotalCost = fakeItemCost(bands[currentIndex].second),
      quotes = quotes,
    ).right()
  }

  private fun fakeItemCost(amount: Double): ItemCost {
    val money = UiMoney(amount, UiCurrencyCode.SEK)
    return ItemCost(monthlyNet = money, monthlyGross = money, discounts = emptyList())
  }
}
