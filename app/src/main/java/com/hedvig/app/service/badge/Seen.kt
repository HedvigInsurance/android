package com.hedvig.app.service.badge

@JvmInline
value class Seen private constructor(val seen: Boolean) {
    companion object {
        fun fromNullableBoolean(seen: Boolean?): Seen = Seen(seen == true)

        fun seen(): Seen = Seen(true)
        fun notSeen(): Seen = Seen(false)
    }
}

fun Seen?.isSeen(): Boolean = this?.seen == true
