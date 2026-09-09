package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.type.MemberPaymentProvider

internal interface RemoveMethodUseCase {
  suspend fun invoke(provider: MemberPaymentProvider): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class RemoveMethodUseCaseImpl : RemoveMethodUseCase {
  override suspend fun invoke(provider: MemberPaymentProvider): Either<ErrorMessage, Unit> {
    // TODO: run the remove-payin-method mutation once the backend exposes one.
    return Unit.right()
  }
}
