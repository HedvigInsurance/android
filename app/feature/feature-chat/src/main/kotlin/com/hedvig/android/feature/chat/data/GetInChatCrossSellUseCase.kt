package com.hedvig.android.feature.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import octopus.InChatCrossSellQuery
import octopus.type.CrossSellInput
import octopus.type.UserFlow

/** The cross-sell the member is offered inside a conversation. */
internal data class InChatCrossSell(
  val id: String,
  val storeUrl: String,
  val discountPercent: Int?,
)

internal interface GetInChatCrossSellUseCase {
  /** Null when there is nothing worth offering this member in chat. */
  suspend fun invoke(): Either<ErrorMessage, InChatCrossSell?>
}

@Inject
internal class GetInChatCrossSellUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetInChatCrossSellUseCase {
  override suspend fun invoke(): Either<ErrorMessage, InChatCrossSell?> = either {
    val data = apolloClient
      .query(InChatCrossSellQuery(inChatCrossSellInput))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .bind()
    val contractGroups = data.currentMember.activeContracts.map {
      it.currentAgreement.productVariant.typeOfContract.toContractGroup()
    }
    // Accident alone does not earn the bundle discount the card promises.
    val onlyHasAccident = contractGroups.isNotEmpty() && contractGroups.all { it == ContractGroup.ACCIDENT }
    if (onlyHasAccident) return@either null
    val recommendation = data.currentMember.crossSellV2.recommendedCrossSell ?: return@either null
    InChatCrossSell(
      id = recommendation.crossSell.id,
      storeUrl = recommendation.crossSell.storeUrl,
      discountPercent = recommendation.discountPercent,
    )
  }
}

@Inject
internal class DemoGetInChatCrossSellUseCase : GetInChatCrossSellUseCase {
  override suspend fun invoke(): Either<ErrorMessage, InChatCrossSell?> = Either.Right(null)
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetInChatCrossSellUseCase>())
internal class SwitchingGetInChatCrossSellUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetInChatCrossSellUseCaseImpl,
  override val demoImpl: DemoGetInChatCrossSellUseCase,
) : GetInChatCrossSellUseCase, DemoSwitcher<GetInChatCrossSellUseCase>() {
  override suspend fun invoke() = pick().invoke()
}

private val inChatCrossSellInput = CrossSellInput(
  // TODO: swap to UserFlow.IN_CHAT_X_SELL once the backend adds it. Quotes from this card are
  //  specified to carry user_flow = in_chat_x_sell; until the enum exists they land under home_x_sell.
  userFlow = UserFlow.HOME_X_SELL,
  flowSource = Optional.absent(),
  experiments = emptyList(),
  contractId = Optional.absent(),
  claimId = Optional.absent(),
)
