package com.hedvig.android.notification.badge.data.storage

import kotlinx.coroutines.flow.Flow

/**
 * A storage which stores which notificationBadges have already been seen by the user and exposes a convenient API to
 * get and set that status.
 */
internal interface NotificationBadgeStorage {
  // https://youtrack.jetbrains.com/issue/KT-31420/Support-JvmName-on-interface-or-provide-other-interface-evolution-mechanism#focus=Comments-27-4062655.0-0
  @Suppress("INAPPLICABLE_JVM_NAME")
  @JvmName("getValueOrEmptySetIfItsNull")
  fun <SetOfT : Set<T>, T> getValue(notificationBadge: NotificationBadge<SetOfT>): Flow<Set<T>>

  fun <T> getValue(notificationBadge: NotificationBadge<T>): Flow<T?>

  suspend fun <T> setValue(notificationBadge: NotificationBadge<T>, newStatus: T)
}
