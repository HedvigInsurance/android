package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.common.ErrorMessage

internal interface SetupSwishPayinUseCase {
  suspend fun invoke(): Either<ErrorMessage, String>
}

internal class SetupSwishPayinUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
): SetupSwishPayinUseCase {
  override suspend fun invoke(): Either<ErrorMessage, String> {
    TODO("Not yet implemented")
  }
}
