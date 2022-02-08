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

    private var resultLauncher: ActivityResultLauncher<DanishAddress?>? = null

    override fun onResume(owner: LifecycleOwner) {
        resultLauncher = registry.register(KEY, FetchDanishAddressContract()) { result ->
            onAddressResult(result)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        resultLauncher?.unregister()
        resultLauncher = null
        super.onPause(owner)
    }

    fun startAutoCompletionActivity(initialAddress: DanishAddress?) {
        resultLauncher?.launch(initialAddress)
    }

    companion object {
        private const val KEY = "com.hedvig.app.feature.addressautocompletion.activityresult.KEY"
    }
}
