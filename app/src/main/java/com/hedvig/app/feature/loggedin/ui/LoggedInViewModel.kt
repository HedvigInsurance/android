package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.type.Feature
import kotlinx.coroutines.launch

abstract class LoggedInViewModel : ViewModel() {
    abstract val data: LiveData<List<Feature>>
    abstract val scroll: MutableLiveData<Float>

    private val _bottomTabInset = MutableLiveData<Int>()
    val bottomTabInset: LiveData<Int>
        get() = _bottomTabInset

    fun updateBottomTabInset(newInset: Int) {
        _bottomTabInset.postValue(newInset)
    }
}

class LoggedInViewModelImpl(
    private val featureRepository: FeatureRepository
) : LoggedInViewModel() {
    override val data = MutableLiveData<List<Feature>>()
    override val scroll = MutableLiveData<Float>()

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
