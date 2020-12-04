package com.hedvig.app.feature.onbarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.EmbarkStoryType
import com.hedvig.app.feature.onbarding.ui.ChoosePlanActivity.Companion.COMBO
import com.hedvig.app.feature.onbarding.ui.ChoosePlanActivity.Companion.CONTENTS
import com.hedvig.app.feature.onbarding.ui.ChoosePlanActivity.Companion.TRAVEL
import kotlinx.coroutines.launch

abstract class ChoosePlanViewModel : ViewModel() {
    protected val _data = MutableLiveData<Result<List<ChoosePlanQuery.EmbarkStory>>>()
    val data: LiveData<Result<List<ChoosePlanQuery.EmbarkStory>>> = _data

    abstract fun load()

    private val _selectedQuoteType = MutableLiveData<OnboardingModel.Bundle>()
    val selectedQuoteType: LiveData<OnboardingModel.Bundle> = _selectedQuoteType

    fun setSelectedQuoteType(type: OnboardingModel.Bundle) {
        _selectedQuoteType.postValue(type)
    }

    fun getSelectedNoPlan() = when {
        _selectedQuoteType.value?.embarkStory?.name?.contains(COMBO) == true -> {
            NoPlan.BUNDLE
        }
        _selectedQuoteType.value?.embarkStory?.name?.contains(CONTENTS) == true -> {
            NoPlan.CONTENT
        }
        _selectedQuoteType.value?.embarkStory?.name?.contains(TRAVEL) == true -> {
            NoPlan.TRAVEL
        }
        else -> {
            NoPlan.BUNDLE
        }
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
            val response = runCatching { repository.getBundles() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { exception ->
                    _data.postValue(Result.failure(exception))
                }
                return@launch
            }
            if (response.getOrNull()?.hasErrors() == true) {
                _data.postValue(Result.failure(Error()))
                return@launch
            }
            val onlyAppStories =
                response.getOrNull()?.data?.embarkStories?.filter { it.type == EmbarkStoryType.APP_ONBOARDING }
            onlyAppStories?.let { _data.postValue(Result.success(it)) }
        }
    }
}
