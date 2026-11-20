@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.data.importantmessages

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.PublicApolloClient
import com.hedvig.android.data.importantmessages.PublicImportantMessage.Link
import com.hedvig.android.data.importantmessages.PublicImportantMessage.Severity
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import octopus.PublicImportantMessagesQuery
import octopus.type.ImportantMessageSeverity

/**
 * The important messages for people who are not logged in, always from the network. A message keeps its link only
 * when the link is an absolute http or https URL with a button text; otherwise it is shown without one.
 */
@HiddenFromObjC
interface GetPublicImportantMessagesUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<PublicImportantMessage>>
}

@Inject
@ContributesBinding(AppScope::class)
internal class GetPublicImportantMessagesUseCaseImpl(
  @PublicApolloClient private val apolloClient: ApolloClient,
) : GetPublicImportantMessagesUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<PublicImportantMessage>> = apolloClient
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
              ?.takeIf { it.url.isAbsoluteWebUrl() && it.buttonText.isNotBlank() }
              ?.let { Link(url = it.url, buttonText = it.buttonText) },
          )
        }
    }
}

private fun String.isAbsoluteWebUrl(): Boolean {
  val afterScheme = when {
    startsWith("https://", ignoreCase = true) -> substring("https://".length)
    startsWith("http://", ignoreCase = true) -> substring("http://".length)
    else -> return false
  }
  val host = afterScheme.takeWhile { it != '/' && it != '?' && it != '#' }
  return host.isNotBlank() && none { it.isWhitespace() }
}
