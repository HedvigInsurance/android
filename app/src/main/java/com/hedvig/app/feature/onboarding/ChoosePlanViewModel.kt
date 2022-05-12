package com.hedvig.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.util.extensions.replace
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class ChoosePlanViewModel(
    private val hAnalytics: HAnalytics,
) : ViewModel() {

    sealed class ViewState {
        data class Success(val bundleItems: List<OnboardingModel.BundleItem>) : ViewState()
        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    sealed class Event {
        data class Continue(val storyName: String, val storyTitle: String) : Event()
    }

    protected val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    abstract fun load()

    fun onBundleSelected(bundleItem: OnboardingModel.BundleItem) {
        (_viewState.value as? ViewState.Success)?.let { viewState ->
            val updatedBundleItems = viewState.bundleItems
                .map { it.copy(selected = false) }
                .replace(bundleItem.copy(selected = true)) { oldItem ->
                    oldItem.bundle.storyName == bundleItem.bundle.storyName
                }
            _viewState.value = ViewState.Success(updatedBundleItems)
        }
    }

    fun onContinue() {
        val selectedBundle = (_viewState.value as? ViewState.Success)?.bundleItems?.firstOrNull { it.selected }?.bundle
        if (selectedBundle != null) {
            hAnalytics.onboardingChooseEmbarkFlow((selectedBundle.storyName))
            _events.trySend(
                Event.Continue(
                    storyName = selectedBundle.storyName,
                    storyTitle = selectedBundle.storyTitle
                )
            )
        }
    }
}

class ChoosePlanViewModelImpl(
    private val getBundlesUseCase: GetBundlesUseCase,
    hAnalytics: HAnalytics,
) : ChoosePlanViewModel(hAnalytics) {

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            val state = when (val result = getBundlesUseCase.invoke()) {
                BundlesResult.Error -> ViewState.Error
                is BundlesResult.Success -> ViewState.Success(result.toModel())
            }
            _viewState.value = state
        }
    }
}

fun BundlesResult.Success.toModel() = bundles.mapIndexed { index, bundle ->
    OnboardingModel.BundleItem(
        selected = index == 0,
        bundle = bundle
    )
}
