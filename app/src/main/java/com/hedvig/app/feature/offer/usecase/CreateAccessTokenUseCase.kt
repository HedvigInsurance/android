package com.hedvig.app.feature.offer.usecase

import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.CreateAccessTokenMutation
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.apollo.safeQuery

class CreateAccessTokenUseCase(
    private val apolloClient: ApolloClient,
    private val authenticationTokenService: AuthenticationTokenService,
) {

    object Success

    @JvmInline
    private value class AccessToken(val token: String)

    suspend operator fun invoke(quoteCartId: QuoteCartId): Either<ErrorMessage, Success> = either {
        val accessToken = query(quoteCartId).bind()
        authenticationTokenService.authenticationToken = accessToken.token
        Success
    }

    private suspend fun query(quoteCartId: QuoteCartId): Either<ErrorMessage, AccessToken> =
        apolloClient
            .mutate(CreateAccessTokenMutation(quoteCartId.id))
            .safeQuery()
            .toEither()
            .mapLeft { ErrorMessage(it.message) }
            .map { AccessToken(it.quoteCart_createAccessToken.accessToken) }
}
