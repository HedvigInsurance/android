package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkMultiActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentBottomSheet.Companion.ADD_COMPONENT_REQUEST_KEY
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MultiActionFragment : Fragment(R.layout.fragment_embark_multi_action), MultiActionAdapter.ClickListener {
    private val model: EmbarkViewModel by sharedViewModel()

    private val multiActionParams: MultiActionParams by lazy {
        requireArguments().getParcelable<MultiActionParams>(DATA)
            ?: throw Error("Programmer error: No PARAMS provided to ${this.javaClass.name}")
    }

    private val multiActionViewModel: MultiActionViewModel by viewModel { parametersOf(multiActionParams) }
    private val binding by viewBinding(FragmentEmbarkMultiActionBinding::bind)

    private val adapter = MultiActionAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(ADD_COMPONENT_REQUEST_KEY) { requestKey: String, bundle: Bundle ->
            if (requestKey == ADD_COMPONENT_REQUEST_KEY) {
                bundle.getParcelable<MultiAction.Component>(AddComponentBottomSheet.RESULT)?.let {
                    multiActionViewModel.onComponentCreated(it)
                }
            }
        }

        binding.apply {
            messages.adapter = MessageAdapter(multiActionParams.messages)
            componentContainer.adapter = adapter
        }

        multiActionViewModel.components.observe(viewLifecycleOwner, adapter::submitList)
        multiActionViewModel.newComponent.observe(viewLifecycleOwner, ::showAddBuildingSheet)
    }

    private fun showAddBuildingSheet(componentState: ComponentState?) {
        AddComponentBottomSheet
            .newInstance(componentState, multiActionParams)
            .show(parentFragmentManager, BOTTOM_SHEET_TAG)
    }

    override fun onComponentClick(id: Long) {
        multiActionViewModel.onComponentClicked(id)
    }

    override fun onComponentRemove(id: Long) {
        multiActionViewModel.onComponentRemoved(id)
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
