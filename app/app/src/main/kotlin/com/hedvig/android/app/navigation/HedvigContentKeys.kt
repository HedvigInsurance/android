package com.hedvig.android.app.navigation

import androidx.navigation3.runtime.NavEntry
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.contentKey

/**
 * Re-stamps every entry [base] produces with [contentKey], replacing navigation3's own default
 * derivation.
 *
 * `NavDisplay` only ever builds entries for the rendered stack, so the keys sitting in
 * `BackstackController.parkedRuns` (and anything restored from `SavedStateRegistry` after process
 * death) have no `NavEntry` to read a content key from. The controller therefore derives its own,
 * and this is the one place that makes entries agree with it. Applying it here rather than at each
 * `entry<>` call site means a new destination cannot forget to opt in, which would silently cost it
 * saved-state and `ViewModel` retention.
 */
internal fun withHedvigContentKeys(
  base: (HedvigNavKey) -> NavEntry<HedvigNavKey>,
): (HedvigNavKey) -> NavEntry<HedvigNavKey> = { key ->
  val entry = base(key)
  NavEntry(
    key = key,
    contentKey = key.contentKey(),
    metadata = entry.metadata,
    content = { entry.Content() },
  )
}
