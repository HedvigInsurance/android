package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkDatePickerBinding
import com.hedvig.app.databinding.FragmentEmbarkMultiActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.home.ui.HomeAdapter
import com.hedvig.app.feature.home.ui.HomeModel
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MultiActionFragment : Fragment(R.layout.fragment_embark_multi_action) {
    private val model: EmbarkViewModel by sharedViewModel()

    private val binding by viewBinding(FragmentEmbarkMultiActionBinding::bind)
    private val data: MultiActionParams
        get() = requireArguments().getParcelable(DATA)
            ?: throw Error("Programmer error: No PARAMS provided to ${this.javaClass.name}")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            messages.adapter = MessageAdapter(data.messages)
            val adapter = MultiActionAdapter()
            componentContainer.adapter = adapter

            adapter.submitList(
                listOf(
                    MultiAction.AddButton(::showAddBuildingSheet),
                    MultiAction.Component("Garage", "36 sqm * water", ::removeComponent)
                ))
        }
    }

    private fun showAddBuildingSheet() {
        AddBuildingBottomSheet
            .newInstance(AddBuildingParams(null, null, false))
            .show(childFragmentManager, "bottomsheet")
    }

    private fun removeComponent() {

    }

    companion object {
        private const val DATA = "DATA"

        fun newInstance(data: MultiActionParams) =
            MultiActionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, data)
                }
            }
    }
}
