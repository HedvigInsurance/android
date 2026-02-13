package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.help.center.model.QuickAction

interface GetQuickLinksUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<QuickAction>>
}
