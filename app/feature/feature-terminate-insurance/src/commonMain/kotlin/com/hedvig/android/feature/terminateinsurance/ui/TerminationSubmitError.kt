package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.runtime.Composable
import com.hedvig.android.design.system.hedvig.ErrorDialog
import hedvig.resources.GENERAL_ERROR_BODY
import hedvig.resources.Res
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

/** Why submitting a termination or withdrawal did not go through. */
internal sealed interface TerminationSubmitError {
  /** The backend refused the request and explained why, in copy meant for the member. */
  data class UserError(val message: String) : TerminationSubmitError

  /** The request failed without a member-facing explanation, e.g. no connection or a GraphQL error. */
  data object Generic : TerminationSubmitError
}

@Composable
internal fun TerminationSubmitErrorDialog(error: TerminationSubmitError, onDismiss: () -> Unit) {
  ErrorDialog(
    title = stringResource(Res.string.something_went_wrong),
    message = when (error) {
      is TerminationSubmitError.UserError -> error.message
      TerminationSubmitError.Generic -> stringResource(Res.string.GENERAL_ERROR_BODY)
    },
    onDismiss = onDismiss,
  )
}
