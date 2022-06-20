package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.CreateAccessTokenMutation
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.apollo.safeQuery

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
            .mutate(CreateAccessTokenMutation(quoteCartId.id))
            .safeQuery()
            .toEither()
            .mapLeft { ErrorMessage(it.message) }
            .map { AccessToken(it.quoteCart_createAccessToken.accessToken) }
}
