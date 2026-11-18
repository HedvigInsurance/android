@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.importantmessages.ios

import androidx.lifecycle.ViewModelProvider
import com.hedvig.android.feature.importantmessages.PublicImportantMessage
import com.hedvig.android.feature.importantmessages.PublicImportantMessagesEvent
import com.hedvig.android.feature.importantmessages.PublicImportantMessagesUiState
import com.hedvig.android.feature.importantmessages.PublicImportantMessagesViewModel
import com.hedvig.android.molecule.public.ScreenModel
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
fun createPublicImportantMessagesScreenModel(
  viewModelFactory: ViewModelProvider.Factory,
): PublicImportantMessagesScreenModel = PublicImportantMessagesScreenModel(viewModelFactory)

/** The important messages shown while logged out. The Swift owner's rules are on [ScreenModel]. */
class PublicImportantMessagesScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) :
  ScreenModel<PublicImportantMessagesScreenState>() {
    private val viewModel =
      viewModel(viewModelFactory, PublicImportantMessagesViewModel::class) { it.toScreenState() }

    /** Loads the messages again. Safe to call from any thread. */
    fun reload() {
      viewModel.send(PublicImportantMessagesEvent.Reload)
    }
  }

internal fun PublicImportantMessagesUiState.toScreenState(): PublicImportantMessagesScreenState =
  PublicImportantMessagesScreenState(messages.map { it.toItem() })

private fun PublicImportantMessage.toItem(): PublicImportantMessageItem = PublicImportantMessageItem(
  id = id,
  message = message,
  severity = when (severity) {
    PublicImportantMessage.Severity.Info -> PublicImportantMessageSeverity.INFO
    PublicImportantMessage.Severity.Warning -> PublicImportantMessageSeverity.WARNING
    PublicImportantMessage.Severity.Error -> PublicImportantMessageSeverity.ERROR
  },
  linkUrl = link?.url,
  linkButtonText = link?.buttonText,
)
