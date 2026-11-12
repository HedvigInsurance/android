package com.hedvig.android.feature.chat.data

import com.eygraber.uri.Uri

/** The platform handle to the content behind the URIs of the files a member sends in the chat. */
internal expect abstract class ChatContentResolver

/**
 * Keeps read access to [uri] across app restarts, so that a message that failed to be sent can be retried later.
 * Returns the exception the platform refused it with, or null when access is kept.
 */
internal expect fun ChatContentResolver.takePersistableReadPermission(uri: Uri): Throwable?

/** Gives up the access kept by [takePersistableReadPermission]. Returns the exception the platform refused it with. */
internal expect fun ChatContentResolver.releasePersistableReadPermission(uri: Uri): Throwable?
