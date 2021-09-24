package com.hedvig.app.feature.insurance.ui.terminatedcontracts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.TerminatedContractsActivityBinding
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.InsuranceAdapter
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TerminatedContractsActivity : BaseActivity(R.layout.terminated_contracts_activity) {
    private val binding by viewBinding(TerminatedContractsActivityBinding::bind)
    private val model: TerminatedContractsViewModel by viewModel()
    private val tracker: InsuranceTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.allowEnterTransitionOverlap = true
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        postponeEnterTransition()
        super.onCreate(savedInstanceState)

        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)
            toolbar.applyStatusBarInsets()
            recycler.applyNavigationBarInsets()
            toolbar.setNavigationOnClickListener { onBackPressed() }
            val adapter = InsuranceAdapter(tracker, marketManager, model::load)
            recycler.adapter = adapter
            model
                .viewState
                .flowWithLifecycle(lifecycle)
                .onEach { viewState ->
                    when (viewState) {
                        TerminatedContractsViewModel.ViewState.Error -> {
                            adapter.submitList(listOf(InsuranceModel.Error))
                        }
                        is TerminatedContractsViewModel.ViewState.Success -> {
                            adapter.submitList(
                                viewState.items
                            )
                            recycler.post { startPostponedEnterTransition() }
                        }
                    }
                }
                .launchIn(lifecycleScope)
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, TerminatedContractsActivity::class.java)
    }
}

class TerminatedContractsViewModel(
    private val getContractsUseCase: GetContractsUseCase,
) : ViewModel() {
    sealed class ViewState {
        data class Success(
            val items: List<InsuranceModel>,
        ) : ViewState()

        object Loading : ViewState()
        object Error : ViewState()
    }

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            when (val result = getContractsUseCase()) {
                is GetContractsUseCase.InsuranceResult.Error -> _viewState.value = ViewState.Error
                is GetContractsUseCase.InsuranceResult.Insurance -> {
                    _viewState.value = ViewState.Success(items(result.insurance))
                }
            }
        }
    }

    init {
        load()
    }
}

private fun items(data: InsuranceQuery.Data): List<InsuranceModel> = data
    .contracts
    .filter { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }
    .map(InsuranceModel::Contract)
