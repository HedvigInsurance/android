package com.hedvig.android.feature.inapppurchase.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.InAppProduct
import com.hedvig.android.data.productvariant.toProductVariant
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.InAppPurchaseQuoteMutation

/**
 * Prices the filled-in form. Call again after any value changes, it opens a new set of offers each time.
 */
internal interface QuoteInAppPurchaseUseCase {
  suspend fun invoke(
    product: InAppProduct,
    fields: List<InAppPurchaseFieldInput>,
  ): Either<ErrorMessage, InAppPurchaseQuote>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class QuoteInAppPurchaseUseCaseImpl(
  private val apolloClient: ApolloClient,
) : QuoteInAppPurchaseUseCase {
  override suspend fun invoke(
    product: InAppProduct,
    fields: List<InAppPurchaseFieldInput>,
  ): Either<ErrorMessage, InAppPurchaseQuote> {
    return either {
      val output = apolloClient
        .mutation(
          InAppPurchaseQuoteMutation(
            product = product.toOctopus(),
            fields = fields.map { octopus.type.InAppPurchaseFieldInput(fieldId = it.fieldId, values = it.values) },
          ),
        )
        .safeExecute()
        .onLeft { logcat(LogPriority.ERROR, it) { "InAppPurchaseQuoteMutation for $product failed" } }
        .mapLeft { ErrorMessage() }
        .bind()
        .inAppPurchaseQuote
      output.userError?.let { raise(ErrorMessage(it.message)) }
      if (output.fieldErrors.isNotEmpty()) {
        return@either InAppPurchaseQuote.InvalidFields(
          output.fieldErrors.map { InAppPurchaseFieldError(it.fieldId, it.message) },
        )
      }
      val shopSessionId = ensureNotNull(output.shopSessionId) {
        logcat(LogPriority.ERROR) { "InAppPurchaseQuoteMutation for $product returned no shopSessionId" }
        ErrorMessage()
      }
      ensure(output.offers.isNotEmpty()) {
        logcat(LogPriority.ERROR) { "InAppPurchaseQuoteMutation for $product returned no offers" }
        ErrorMessage()
      }
      InAppPurchaseQuote.Quoted(
        shopSessionId = shopSessionId,
        offers = output.offers.map { offer ->
          val variant = offer.variant.toProductVariant()
          val cost = InAppPurchaseOfferCost(
            baseMonthlyGross = UiMoney.fromMoneyFragment(offer.cost.baseInsuranceGross),
            monthlyGross = UiMoney.fromMoneyFragment(offer.cost.gross),
            monthlyNet = UiMoney.fromMoneyFragment(offer.cost.net),
            discounts = offer.cost.discountsV2.map {
              InAppPurchaseDiscount(it.itemDiscount.displayName, UiMoney.fromMoneyFragment(it.amount))
            },
          )
          InAppPurchaseOffer(
            id = offer.id,
            variant = variant,
            exposureDisplayName = offer.exposure.displayNameShort,
            cost = cost,
            costBreakdown = costBreakdownOf(variant.displayName, cost),
            displayItems = offer.displayItems.map { InAppPurchaseDisplayItem(it.displayTitle, it.displayValue) },
            deductible = offer.deductible?.let { InAppPurchaseDeductible(it.displayName, it.tagline) },
            insuranceAmount = offer.insuranceAmount?.let { InAppPurchaseInsuranceAmount(it.displayName, it.amount) },
            usps = offer.usps,
            startDate = offer.startDate,
          )
        },
      )
    }
  }
}
