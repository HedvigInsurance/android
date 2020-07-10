package com.hedvig.app.util.apollo

import com.apollographql.apollo.Logger
import d
import e
import w

class ApolloTimberLogger : Logger {
    override fun log(priority: Int, message: String, t: Throwable?, vararg args: Any) {
        when (priority) {
            Logger.DEBUG -> {
                if (t != null) {
                    d(t) { String.format(message, *args) }
                } else {
                    d { String.format(message, *args) }
                }
            }
            Logger.WARN -> {
                if (t != null) {
                    w(t) { String.format(message, *args) }
                } else {
                    w { String.format(message, *args) }
                }
            }
            Logger.ERROR -> {
                if (t != null) {
                    e(t) { String.format(message, *args) }
                } else {
                    e { String.format(message, *args) }
                }
            }
        }
    }
}
