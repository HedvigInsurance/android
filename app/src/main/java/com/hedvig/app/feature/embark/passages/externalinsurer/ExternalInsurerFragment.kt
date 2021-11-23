package com.hedvig.app.feature.embark.passages.externalinsurer

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.PreviousOrExternalInsurerFragmentBinding
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.AskForPriceInfoActivity
import com.hedvig.app.feature.embark.passages.externalinsurer.askforprice.AskForPriceInfoParameter
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerBottomSheet
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.whenApiVersion
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ExternalInsurerFragment : Fragment(R.layout.previous_or_external_insurer_fragment) {

    private val binding by viewBinding(PreviousOrExternalInsurerFragmentBinding::bind)
    private val viewModel: ExternalInsurerViewModel by sharedViewModel()

    private val insurerData by lazy {
        requireArguments()
            .getParcelable<ExternalInsurerParameter>(DATA)
            ?: throw IllegalArgumentException("No argument passed to ${this.javaClass.name}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.apply {
            whenApiVersion(Build.VERSION_CODES.R) {
                currentInsurerContainer.setupInsetsForIme(
                    root = root,
                    currentInsurerContainer,
                )
            }

            messages.adapter = MessageAdapter(insurerData.messages)

            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }
        }
    }

    private fun startAskForPrice() {
        AskForPriceInfoActivity.createIntent(
            requireContext(),
            AskForPriceInfoParameter("testId")
        )
    }

    private fun onShowInsurers() {
        val fragment = PreviousInsurerBottomSheet.newInstance(listOf())
        fragment.show(parentFragmentManager, PreviousInsurerBottomSheet.TAG)
    }

    companion object {
        private const val DATA = "DATA"

        fun newInstance(previousInsurerData: ExternalInsurerParameter) =
            ExternalInsurerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, previousInsurerData)
                }
            }
    }
}
