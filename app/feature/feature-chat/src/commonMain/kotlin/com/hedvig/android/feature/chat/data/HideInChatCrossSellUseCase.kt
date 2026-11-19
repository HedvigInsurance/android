package com.hedvig.android.feature.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.benasher44.uuid.Uuid
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import octopus.ConversationHideCrossSalesMutation

internal interface HideInChatCrossSellUseCase {
  /** Stops offering cross-sells in this conversation, for good. */
  suspend fun invoke(conversationId: Uuid): Either<ErrorMessage, Unit>
}

@Inject
internal class HideInChatCrossSellUseCaseImpl(
  private val apolloClient: ApolloClient,
) : HideInChatCrossSellUseCase {
  override suspend fun invoke(conversationId: Uuid): Either<ErrorMessage, Unit> = either {
    apolloClient
      .mutation(ConversationHideCrossSalesMutation(conversationId.toString()))
      .safeExecute(::ErrorMessage)
      .bind()
  }
}

@Inject
internal class DemoHideInChatCrossSellUseCase : HideInChatCrossSellUseCase {
  override suspend fun invoke(conversationId: Uuid): Either<ErrorMessage, Unit> = Either.Right(Unit)
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<HideInChatCrossSellUseCase>())
internal class SwitchingHideInChatCrossSellUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: HideInChatCrossSellUseCaseImpl,
  override val demoImpl: DemoHideInChatCrossSellUseCase,
) : HideInChatCrossSellUseCase, DemoSwitcher<HideInChatCrossSellUseCase>() {
  override suspend fun invoke(conversationId: Uuid) = pick().invoke(conversationId)
}
