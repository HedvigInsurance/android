package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkMultiActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.Response
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet.Companion.ADD_COMPONENT_REQUEST_KEY
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_MILLIS
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dev.chrisbanes.insetter.Insetter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.parameter.parametersOf

class MultiActionFragment : Fragment(R.layout.fragment_embark_multi_action) {
    private val model: EmbarkViewModel by sharedViewModel()

    private val multiActionParams: MultiActionParams by lazy {
        requireArguments().getParcelable<MultiActionParams>(DATA)
            ?: throw Error("Programmer error: No PARAMS provided to ${this.javaClass.name}")
    }

    private val multiActionViewModel: MultiActionViewModel by sharedViewModel { parametersOf(multiActionParams) }
    private val binding by viewBinding(FragmentEmbarkMultiActionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        Insetter.builder().setOnApplyInsetsListener { insetView, insets, initialState ->
            insetView.updateMargin(bottom = initialState.paddings.bottom + insets.stableInsetBottom)
        }.applyToView(binding.continueButton)

        setFragmentResultListener(ADD_COMPONENT_REQUEST_KEY) { requestKey: String, bundle: Bundle ->
            if (requestKey == ADD_COMPONENT_REQUEST_KEY) {
                bundle.getParcelable<MultiActionItem.Component>(AddComponentBottomSheet.RESULT)?.let {
                    multiActionViewModel.onComponentCreated(it)
                }
            }
        }

        val adapter = MultiActionAdapter(
            multiActionViewModel::onComponentClicked,
            multiActionViewModel::onComponentRemoved
        )
        binding.apply {
            messages.adapter = MessageAdapter(multiActionParams.messages)
            componentContainer.adapter = adapter
            continueButton.text = multiActionParams.addLabel

            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }
        }

        multiActionViewModel.components.observe(viewLifecycleOwner, adapter::submitList)
        multiActionViewModel.newComponent.observe(viewLifecycleOwner, ::showAddBuildingSheet)

        binding.continueButton
            .hapticClicks()
            .mapLatest { saveAndAnimate() }
            .onEach {
                model.navigateToPassage(multiActionParams.link)
            }
            .launchIn(viewLifecycleScope)
    }

    private suspend fun saveAndAnimate() {
        multiActionViewModel.onContinue(model::putInStore)
        val response =
            model.preProcessResponse(multiActionParams.passageName) ?: Response.SingleResponse("")
        animateResponse(binding.responseContainer, response)
        delay(PASSAGE_ANIMATION_DELAY_MILLIS)
    }

    private fun showAddBuildingSheet(componentState: MultiActionItem.Component?) {
        AddComponentBottomSheet
            .newInstance(componentState, multiActionParams)
            .show(parentFragmentManager, BOTTOM_SHEET_TAG)
    }

    companion object {
        private const val DATA = "DATA"
        private const val BOTTOM_SHEET_TAG = "BOTTOM_SHEET_TAG"

        fun newInstance(data: MultiActionParams) =
            MultiActionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, data)
                }
            }
    }
}
