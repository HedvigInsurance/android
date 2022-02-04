package com.hedvig.app.feature.addressautocompletion.activityresult

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress

/**
 * Must remember to register as a lifecycle observer in the `onCreate` method of the activity.
 * More information: https://developer.android.com/training/basics/intents/result#separate
 */
class FetchDanishAddressAutoCompleteContractHandler(
    private val registry: ActivityResultRegistry,
    private val onAddressResult: (result: FetchDanishAddressContractResult) -> Unit,
) : DefaultLifecycleObserver {

    private lateinit var resultLauncher: ActivityResultLauncher<DanishAddress?>

    override fun onCreate(owner: LifecycleOwner) {
        resultLauncher = registry.register(
            "com.hedvig.app.feature.addressautocompletion.activityresult.FetchDanishAddressAutoCompleteContractHandler",
            owner,
            FetchDanishAddressContract()
        ) { result ->
            onAddressResult(result)
        }
    }

    fun startAutoCompletionActivity(initialAddress: DanishAddress?) {
        resultLauncher.launch(initialAddress)
    }
}
