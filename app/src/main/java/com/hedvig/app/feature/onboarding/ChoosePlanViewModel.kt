package com.hedvig.app.feature.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.EmbarkStoryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class ChoosePlanViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(val data: List<ChoosePlanQuery.EmbarkStory>) : ViewState()
        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _data = MutableStateFlow<ViewState>(ViewState.Loading)
    val data = _data.asStateFlow()

    abstract fun load()

    private val _selectedQuoteType = MutableLiveData<OnboardingModel.Bundle>()
    val selectedQuoteType: LiveData<OnboardingModel.Bundle> = _selectedQuoteType

    fun setSelectedQuoteType(type: OnboardingModel.Bundle) {
        _selectedQuoteType.postValue(type)
    }
}

class ChoosePlanViewModelImpl(
    private val repository: ChoosePlanRepository
) : ChoosePlanViewModel() {
    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            val response = runCatching { repository.bundles() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let {
                    _data.value = ViewState.Error
                }
                return@launch
            }
            if (response.getOrNull()?.hasErrors() == true) {
                _data.value = ViewState.Error
                return@launch
            }
            val onlyAppStories =
                response.getOrNull()?.data?.embarkStories?.filter { it.type == EmbarkStoryType.APP_ONBOARDING }
            onlyAppStories?.let { _data.value = ViewState.Success(it) }
        }
    }
}
