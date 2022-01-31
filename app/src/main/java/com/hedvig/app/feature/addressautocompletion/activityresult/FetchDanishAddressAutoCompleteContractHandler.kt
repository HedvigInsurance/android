package com.hedvig.app.feature.addressautocompletion.activityresult

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.launch
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Must remember to register as a lifecycle observer in the `onCreate` method of the activity.
 * More information: https://developer.android.com/training/basics/intents/result#separate
 */
class FetchDanishAddressAutoCompleteContractHandler(
    private val registry: ActivityResultRegistry,
    private val onAddressResult: (address: String) -> Unit,
) : DefaultLifecycleObserver {

    private lateinit var resultLauncher: ActivityResultLauncher<Unit>

    override fun onCreate(owner: LifecycleOwner) {
        resultLauncher = registry.register(
            "key",
            owner,
            FetchDanishAddressContract()
        ) { result ->
            if (result == null) return@register
            onAddressResult(result)
        }
    }

    fun startAutoCompletionActivity() {
        resultLauncher.launch()
    }
}
