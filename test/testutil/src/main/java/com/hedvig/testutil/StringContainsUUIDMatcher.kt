package com.hedvig.testutil

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class StringContainsUUIDMatcher : TypeSafeMatcher<String>() {
    override fun describeTo(description: Description?) {
        description?.appendText("a string containing a UUID")
    }

    override fun matchesSafely(item: String?): Boolean =
        item?.let { UUID_REGEX.containsMatchIn(it) } == true

    companion object {
        fun containsUUID() = StringContainsUUIDMatcher()

        private val UUID_REGEX = Regex("[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")
    }
}
