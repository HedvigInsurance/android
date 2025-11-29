package com.hedvig.android.compose.ui

/**
 * Used when the content description is purposefully left as null as we either want to ignore this item from a11y, or
 * we have some other mechanism in order to read this component properly.
 * This helps us differentiate these instances from
 */
val EmptyContentDescription: String? = null
