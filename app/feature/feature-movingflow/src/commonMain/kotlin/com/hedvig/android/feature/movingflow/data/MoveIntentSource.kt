package com.hedvig.android.feature.movingflow.data

import com.apollographql.apollo.api.Optional
import com.hedvig.android.feature.movingflow.MovingSource
import octopus.type.MoveIntentSourceInput

/**
 * The backend only models the entry points it acts on, so [MovingSource.OTHER] is sent as absent, which the backend
 * reads as [MoveIntentSourceInput.INSURANCE].
 */
internal fun MovingSource.toMoveIntentSourceInput(): Optional<MoveIntentSourceInput> {
  return when (this) {
    MovingSource.INSURANCE -> Optional.present(MoveIntentSourceInput.INSURANCE)
    MovingSource.TERMINATION -> Optional.present(MoveIntentSourceInput.TERMINATION)
    MovingSource.OTHER -> Optional.absent()
  }
}
