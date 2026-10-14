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

internal interface GetInAppPurchaseFormUseCase {
  suspend fun invoke(product: InAppProduct): Either<ErrorMessage, InAppPurchaseForm>
}

// query InAppPurchaseForm($product: InAppProduct!) {
//   inAppPurchaseForm(product: $product) {
//     product {
//       name
//       displayNameShort
//       displayNameFull
//       tagline
//       pillowImage { src alt }
//       featuredImage { src alt }
//       variants { ...ProductVariantFragment }
//       faq { question answerMarkdown }
//     }
//     eligibility {
//       isEligible
//       reason
//       existingContractId
//     }
//     fields {
//       id
//       type
//       title
//       options { value title isSelectable additionalInfo }
//       defaultValues
//       minValue
//       maxValue
//     }
//   }
// }
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetInAppPurchaseFormUseCaseImpl(
  @Suppress("unused") private val apolloClient: ApolloClient,
) : GetInAppPurchaseFormUseCase {
  override suspend fun invoke(product: InAppProduct): Either<ErrorMessage, InAppPurchaseForm> {
    return either {
      // val form = apolloClient
      //   .query(InAppPurchaseFormQuery(product = product.toOctopus()))
      //   .fetchPolicy(FetchPolicy.NetworkOnly)
      //   .safeExecute()
      //   .onLeft { logcat(LogPriority.ERROR, it) { "InAppPurchaseFormQuery for $product failed" } }
      //   .mapLeft { ErrorMessage() }
      //   .bind()
      //   .inAppPurchaseForm
      // InAppPurchaseForm(
      //   product = with(form.product) {
      //     InAppPurchaseProduct(
      //       name = name,
      //       displayNameShort = displayNameShort,
      //       displayNameFull = displayNameFull,
      //       tagline = tagline,
      //       pillowImage = InAppPurchaseImage(pillowImage.src, pillowImage.alt),
      //       featuredImage = featuredImage?.let { InAppPurchaseImage(it.src, it.alt) },
      //       variants = variants.map { it.toProductVariant() },
      //       faq = faq.map { InAppPurchaseFaqItem(it.question, it.answerMarkdown) },
      //     )
      //   },
      //   eligibility = if (form.eligibility.isEligible) {
      //     InAppPurchaseEligibility.Eligible
      //   } else {
      //     InAppPurchaseEligibility.NotEligible(
      //       reason = when (form.eligibility.reason) {
      //         octopus.type.InAppPurchaseIneligibilityReason.ALREADY_INSURED -> {
      //           InAppPurchaseIneligibilityReason.ALREADY_INSURED
      //         }
      //
      //         else -> InAppPurchaseIneligibilityReason.NOT_AVAILABLE
      //       },
      //       existingContractId = form.eligibility.existingContractId,
      //     )
      //   },
      //   fields = form.fields.mapNotNull { it.toFormField() },
      // )
      delay(MockNetworkDelay)
      when (product) {
        InAppProduct.ACCIDENT -> InAppPurchaseMocks.accidentForm
        InAppProduct.PAYMENT_PROTECTION -> InAppPurchaseMocks.paymentProtectionForm
      }
    }
  }
}

// internal fun InAppProduct.toOctopus(): octopus.type.InAppProduct = when (this) {
//   InAppProduct.ACCIDENT -> octopus.type.InAppProduct.ACCIDENT
//   InAppProduct.PAYMENT_PROTECTION -> octopus.type.InAppProduct.PAYMENT_PROTECTION
// }
//
// A field of a type this app version doesn't know is dropped, the quote then reports it in fieldErrors.
// private fun InAppPurchaseFormQuery.Data.InAppPurchaseForm.Field.toFormField(): InAppPurchaseFormField? {
//   return when (type) {
//     InAppPurchaseFormFieldType.NUMBER -> InAppPurchaseFormField.Number(
//       id = id,
//       title = title,
//       defaultValue = defaultValues.firstOrNull()?.toIntOrNull(),
//       minValue = minValue?.toIntOrNull(),
//       maxValue = maxValue?.toIntOrNull(),
//     )
//
//     InAppPurchaseFormFieldType.DATE -> InAppPurchaseFormField.Date(
//       id = id,
//       title = title,
//       defaultValue = defaultValues.firstOrNull()?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
//       minValue = minValue?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
//       maxValue = maxValue?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
//     )
//
//     InAppPurchaseFormFieldType.SINGLE_SELECT -> InAppPurchaseFormField.SingleSelect(
//       id = id,
//       title = title,
//       options = options.orEmpty().map {
//         InAppPurchaseFormField.SingleSelect.Option(it.value, it.title, it.isSelectable, it.additionalInfo)
//       },
//       defaultValue = defaultValues.firstOrNull(),
//     )
//
//     InAppPurchaseFormFieldType.UNKNOWN__ -> {
//       logcat(LogPriority.ERROR) { "InAppPurchaseForm field $id has unknown type ${type.rawValue}" }
//       null
//     }
//   }
// }
