package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.CreateAccessTokenMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage

interface CreateAccessTokenUseCase {
  object Success

  suspend fun invoke(quoteCartId: QuoteCartId): Either<ErrorMessage, Success>
}

class CreateAccessTokenUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val authenticationTokenService: AuthenticationTokenService,
) : CreateAccessTokenUseCase {
  @JvmInline
  private value class AccessToken(val token: String)

  override suspend fun invoke(
    quoteCartId: QuoteCartId,
  ): Either<ErrorMessage, CreateAccessTokenUseCase.Success> = either {
    val accessToken = query(quoteCartId).bind()
    authenticationTokenService.authenticationToken = accessToken.token
    CreateAccessTokenUseCase.Success
  }

  private suspend fun query(quoteCartId: QuoteCartId): Either<ErrorMessage, AccessToken> =
    apolloClient
      .mutation(CreateAccessTokenMutation(quoteCartId.id))
      .safeExecute()
      .toEither()
      .mapLeft { ErrorMessage(it.message) }
      .map { AccessToken(it.quoteCart_createAccessToken.accessToken) }
}
