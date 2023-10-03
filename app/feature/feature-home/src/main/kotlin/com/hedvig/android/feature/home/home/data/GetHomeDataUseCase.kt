package com.hedvig.android.feature.home.home.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.Flow

internal interface GetHomeDataUseCase {
  fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, HomeData>>
}
