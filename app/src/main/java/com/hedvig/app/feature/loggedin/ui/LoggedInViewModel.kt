package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.type.Feature
import kotlinx.coroutines.launch

abstract class LoggedInViewModel : ViewModel() {
    // TODO: Data type
    abstract val data: LiveData<List<Feature>>
}

class LoggedInViewModelImpl(
    private val featureRepository: FeatureRepository
) : LoggedInViewModel() {
    override val data = MutableLiveData<List<Feature>>()

    init {
        viewModelScope.launch {
            val response = runCatching {
                featureRepository
                    .featuresAsync()
                    .await()
            }

            data.postValue(response.getOrNull()?.data()?.member?.features)
        }
    }
}
