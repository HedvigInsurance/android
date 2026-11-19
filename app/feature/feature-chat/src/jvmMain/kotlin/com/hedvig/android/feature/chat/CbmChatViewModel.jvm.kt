package com.hedvig.android.feature.chat

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.molecule.public.mainDispatcher
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext

internal actual val chatViewModelDispatcher: CoroutineContext = mainDispatcher

internal actual abstract class ChatContext

@Inject
@ContributesBinding(AppScope::class)
internal class DesktopChatContext : ChatContext()

/** The desktop app does not read the accessibility settings of the operating system. */
internal actual fun isAccessibilityEnabled(context: ChatContext): Boolean = false
