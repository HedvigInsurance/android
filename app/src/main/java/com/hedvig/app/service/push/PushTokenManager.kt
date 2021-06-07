package com.hedvig.app.service.push

import com.google.firebase.messaging.FirebaseMessaging
import com.hedvig.app.util.extensions.await

class PushTokenManager(
    private val firebaseMessaging: FirebaseMessaging,
) {
    suspend fun refreshToken() {
        firebaseMessaging.deleteToken().await()
        firebaseMessaging.token.await()
    }
}
