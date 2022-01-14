package com.hedvig.app.util

import android.text.format.DateUtils
import java.time.Instant

object HedvigDateUtils {

    /**
     * Returns a localized string, like:
     * 7 or more days ago  -> English: Dec 16, 2021   Swedish: 16 dec. 2021
     * 6 days ago          -> English: 6 days ago     Swedish: För 6 dagar sedan
     * 3 days ago          -> English: 3 days ago     Swedish: För 3 dagar sedan
     * 2 days ago          -> English: 2 days ago     Swedish: I förrgår
     * 1 day ago           -> English: Yesterday      Swedish: I går
     * 10 hours ago        -> English: 10 hours ago   Swedish: För 10 timmar sedan
     * 30 minutes ago      -> English: 30 minutes ago Swedish: För 30 minuter sedan
     * 30 seconds ago      -> English: 30 seconds ago Swedish: För 30 sekunder sedan
     *
     * DateUtils look at locale from what was set at Configuration(context.resources.configuration).setLocale()
     * Also looks at what was set with Locale.setDefault() so if that is called without changing the local
     * configuration it *will* show the wrong locale compared to the rest of the screen, so we have to make sure those
     * two are the same.
     */
    fun getRelativeTimeSpanString(
        from: Instant,
        to: Instant = Instant.now(),
    ): String {
        return DateUtils.getRelativeTimeSpanString(
            from.toEpochMilli(),
            to.toEpochMilli(),
            DateUtils.SECOND_IN_MILLIS
        ).toString()
    }
}
