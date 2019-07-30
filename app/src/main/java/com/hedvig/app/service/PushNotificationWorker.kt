package com.hedvig.app.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.RegisterPushTokenMutation
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.extensions.getAuthenticationToken
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class PushNotificationWorker(
    val context: Context,
    params: WorkerParameters
) : Worker(context, params), KoinComponent {

    private val apolloClientWrapper: ApolloClientWrapper by inject()

    private val disposables = CompositeDisposable()

    override fun doWork(): Result {
        val pushToken = inputData.getString(PUSH_TOKEN) ?: throw Exception("No token provided")
        if (!hasHedvigToken()) {
            return Result.retry()
        }
        registerPushToken(pushToken)
        return Result.success()
    }

    private fun hasHedvigToken(): Boolean {
        try {
            val hedvigToken = context.getAuthenticationToken()
            if (hedvigToken != null) {
                return true
            }
        } catch (exception: Exception) {
            Timber.e(exception)
        }
        return false
    }

    private fun registerPushToken(pushToken: String) {
        Timber.i("Registering push token")
        val registerPushTokenMutation = RegisterPushTokenMutation
            .builder()
            .pushToken(pushToken)
            .build()

        disposables += Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(registerPushTokenMutation))
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e("Failed to handleExpandWithKeyboard push token: %s", response.errors().toString())
                    return@subscribe
                }
                Timber.i("Successfully registered push token")
            }, { Timber.e(it, "Failed to handleExpandWithKeyboard push token") })
    }

    companion object {
        const val PUSH_TOKEN = "push_token"
    }
}

