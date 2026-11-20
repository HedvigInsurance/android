@file:OptIn(ExperimentalObjCRefinement::class, ExperimentalObjCEnum::class)

package com.hedvig.android.data.importantmessages.ios

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.importantmessages.GetPublicImportantMessagesUseCase
import com.hedvig.android.data.importantmessages.PublicImportantMessage
import com.hedvig.android.iosdata.bridge.IosGraphHolder
import com.hedvig.android.iosdata.bridge.component
import com.hedvig.android.iosdata.bridge.iosRead
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import kotlin.experimental.ExperimentalObjCEnum
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCEnum

/** The important messages the iOS app shows before login. */
@Inject
class PublicImportantMessagesReads internal constructor(
  private val getPublicImportantMessages: GetPublicImportantMessagesUseCase,
) {
  /** The active messages in the app's language, in the order the backend sends them, which is most severe first. */
  @NativeCoroutines
  suspend fun messages(): List<PublicImportantMessageItem> = iosRead {
    getPublicImportantMessages.invoke().bind().map { it.toItem() }
  }
}

class PublicImportantMessageItem(
  val id: String,
  val message: String,
  val severity: PublicImportantMessageSeverity,
  /** Set together with [linkButtonText], or both are null. */
  val linkUrl: String?,
  val linkButtonText: String?,
)

@ObjCEnum
enum class PublicImportantMessageSeverity {
  INFO,
  WARNING,
  ERROR,
}

private fun PublicImportantMessage.toItem() = PublicImportantMessageItem(
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

@HiddenFromObjC
@ContributesTo(AppScope::class)
interface PublicImportantMessagesReadsComponent {
  val publicImportantMessagesReads: PublicImportantMessagesReads
}

fun IosGraphHolder.publicImportantMessagesReads(): PublicImportantMessagesReads =
  component<PublicImportantMessagesReadsComponent>().publicImportantMessagesReads
