package com.hedvig.android.core.appreview

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

/**
 * This function will suspend and wait forever until the first time we should open the app review dialog.
 */
interface WaitUntilAppReviewDialogShouldBeOpenedUseCase {
  suspend fun invoke()
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class WaitUntilAppReviewDialogShouldBeOpenedUseCaseImpl(
  private val dataStore: SelfServiceCompletedEventStore,
) : WaitUntilAppReviewDialogShouldBeOpenedUseCase {
  override suspend fun invoke() {
    dataStore.observeNumberOfCompletedSelfServices()
      // Drop the first number, whatever it was, to not show a dialog right as the app is launched. We are only
      // interested in observing changes to this number after we've already started observing it
      .drop(1)
      .filter { it > 0 }
      .first()
    dataStore.resetSelfServiceCompletions()
  }
}
