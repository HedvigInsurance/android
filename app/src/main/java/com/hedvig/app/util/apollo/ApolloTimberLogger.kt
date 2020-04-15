package com.hedvig.app.util.apollo

import com.apollographql.apollo.Logger
import com.apollographql.apollo.api.internal.Optional
import d
import e
import w

class ApolloTimberLogger : Logger {
    override fun log(priority: Int, message: String, t: Optional<Throwable>, vararg args: Any) {
        when (priority) {
            Logger.DEBUG -> {
                if (t.isPresent) {
                    d(t.get())
                } else {
                    d { "$message $args" }
                }
            }
            Logger.WARN -> {
                if (t.isPresent) {
                    w(t.get())
                } else {
                    w { "$message, $args" }
                }
            }
            Logger.ERROR -> {
                if (t.isPresent) {
                    e(t.get())
                } else {
                    e { "$message $args" }
                }
            }
        }
    }
}
