package com.hedvig.app.util.apollo

import com.apollographql.apollo.Logger
import com.apollographql.apollo.api.internal.Optional
import timber.log.Timber

class ApolloTimberLogger : Logger {
    override fun log(priority: Int, message: String, t: Optional<Throwable>, vararg args: Any) {
        when (priority) {
            Logger.DEBUG -> {
                if (t.isPresent) {
                    Timber.d(t.get())
                } else {
                    Timber.d(message, *args)
                }
            }
            Logger.WARN -> {
                if (t.isPresent) {
                    Timber.w(t.get())
                } else {
                    Timber.w(message, *args)
                }
            }
            Logger.ERROR -> {
                if (t.isPresent) {
                    Timber.e(t.get())
                } else {
                    Timber.e(message, *args)
                }
            }
        }
    }
}
