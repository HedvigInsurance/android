package com.hedvig.android.feature.product.inapp.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.data.contract.InAppProduct
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.CrossSellProductInfoQuery

internal interface GetCrossSellProductInfoUseCase {
  suspend fun invoke(crossSellId: String): Either<ApolloOperationError, CrossSellProductInfo>
}

@ContributesBinding(AppScope::class)
@Inject
internal class GetCrossSellProductInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellProductInfoUseCase {
  override suspend fun invoke(crossSellId: String): Either<ApolloOperationError, CrossSellProductInfo> {
    return apolloClient
      .query(CrossSellProductInfoQuery(crossSellId))
      .safeExecute()
      .map { data -> data.currentMember.crossSellProductInfo.toProductInfo() }
      .onLeft { error ->
        logcat(operationError = error) { "GetCrossSellProductInfoUseCase failed for crossSellId:$crossSellId" }
      }
  }
}

private fun CrossSellProductInfoQuery.Data.CurrentMember.CrossSellProductInfo.toProductInfo(): CrossSellProductInfo {
  return CrossSellProductInfo(
    heroImage = ImageAsset(id = heroImage.id, src = heroImage.src, description = heroImage.alt),
    heroIntro = heroIntro?.let { CrossSellProductInfo.HeroIntro(title = it.title, description = it.description) },
    usps = usps.map { CrossSellProductInfo.Usp(title = it.title, description = it.description) },
    carousel = carousel.map { item ->
      CrossSellProductInfo.CarouselItem(
        title = item.title,
        subtitle = item.subtitle,
        image = ImageAsset(id = item.image.id, src = item.image.src, description = item.image.alt),
      )
    },
    inAppProduct = inAppProduct?.toInAppProduct(),
    faq = faq.map { faqItem(it.question, it.answer) },
  )
}

internal fun octopus.type.InAppProduct.toInAppProduct(): InAppProduct? = when (this) {
  octopus.type.InAppProduct.ACCIDENT -> InAppProduct.ACCIDENT
  octopus.type.InAppProduct.PAYMENT_PROTECTION -> InAppProduct.PAYMENT_PROTECTION
  octopus.type.InAppProduct.DOG -> InAppProduct.DOG
  octopus.type.InAppProduct.CAT -> InAppProduct.CAT
  octopus.type.InAppProduct.CAR -> InAppProduct.CAR
  octopus.type.InAppProduct.APARTMENT_RENT -> InAppProduct.APARTMENT_RENT
  octopus.type.InAppProduct.APARTMENT_BRF -> InAppProduct.APARTMENT_BRF
  octopus.type.InAppProduct.APARTMENT_STUDENT -> InAppProduct.APARTMENT_STUDENT
  octopus.type.InAppProduct.HOUSE -> InAppProduct.HOUSE
  octopus.type.InAppProduct.VACATION_HOME -> InAppProduct.VACATION_HOME
  octopus.type.InAppProduct.UNKNOWN__ -> null
}

internal fun faqItem(question: String, answer: Any?) =
  CrossSellProductInfo.FaqItem(question = question, answerMarkdown = storyblokRichTextToMarkdown(answer))
