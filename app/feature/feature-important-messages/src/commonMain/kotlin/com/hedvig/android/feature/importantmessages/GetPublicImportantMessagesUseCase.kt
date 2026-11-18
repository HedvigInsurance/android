package com.hedvig.android.feature.importantmessages

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.PublicApolloClient
import com.hedvig.android.feature.importantmessages.PublicImportantMessage.Link
import com.hedvig.android.feature.importantmessages.PublicImportantMessage.Severity
import dev.zacsweers.metro.Inject
import octopus.PublicImportantMessagesQuery
import octopus.type.ImportantMessageSeverity

@Inject
internal class GetPublicImportantMessagesUseCase(
  @PublicApolloClient private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): Either<ErrorMessage, List<PublicImportantMessage>> = apolloClient
    .query(PublicImportantMessagesQuery())
    .fetchPolicy(FetchPolicy.NetworkOnly)
    .safeExecute(::ErrorMessage)
    .map { data ->
      data.publicImportantMessages
        .filter { it.message.isNotBlank() }
        .map { message ->
          PublicImportantMessage(
            id = message.id,
            message = message.message,
            severity = when (message.severity) {
              ImportantMessageSeverity.INFO -> Severity.Info
              ImportantMessageSeverity.WARNING -> Severity.Warning
              ImportantMessageSeverity.ERROR -> Severity.Error
              ImportantMessageSeverity.UNKNOWN__ -> Severity.Warning
            },
            link = message.linkInfo
              ?.takeIf { it.url.isNotBlank() }
              ?.let { Link(url = it.url, buttonText = it.buttonText) },
          )
        }
    }
}
