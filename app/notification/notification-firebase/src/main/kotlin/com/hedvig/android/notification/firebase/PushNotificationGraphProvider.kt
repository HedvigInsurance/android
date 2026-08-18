package com.hedvig.android.notification.firebase

/**
 * Bridge implemented by the Application so that the framework-instantiated [PushNotificationService]
 * can reach the root Metro graph (which lives in `:app` and is not visible from this module) for
 * member injection.
 */
interface PushNotificationGraphProvider {
  fun inject(service: PushNotificationService)
}
