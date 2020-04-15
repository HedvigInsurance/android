package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.util.extensions.safeLaunch

abstract class LoggedInViewModel : ViewModel() {
    abstract val data: LiveData<List<Feature>>
}

class LoggedInViewModelImpl(
    private val featureRepository: FeatureRepository
) : LoggedInViewModel() {
    override val data = MutableLiveData<List<Feature>>()

    init {
        viewModelScope.safeLaunch {
            val response = runCatching {
                featureRepository
                    .featuresAsync()
                    .await()
            }

            data.postValue(response.getOrNull()?.data()?.member?.features)
        }
    }
}
