package com.hedvig.android.feature.chat.data

import com.eygraber.uri.Uri
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

internal actual abstract class ChatContentResolver

@Inject
@ContributesBinding(AppScope::class)
internal class DesktopChatContentResolver : ChatContentResolver()

/** A file picked on the desktop stays readable at its path, so there is no access to keep. */
internal actual fun ChatContentResolver.takePersistableReadPermission(uri: Uri): Throwable? = null

internal actual fun ChatContentResolver.releasePersistableReadPermission(uri: Uri): Throwable? = null
