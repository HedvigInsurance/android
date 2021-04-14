package com.hedvig.app.feature.embark.passages.previousinsurer

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.PreviousInsurerFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.whenApiVersion
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PreviousInsurerFragment : Fragment(R.layout.previous_insurer_fragment) {

    private val binding by viewBinding(PreviousInsurerFragmentBinding::bind)
    private val model: EmbarkViewModel by sharedViewModel()
    private val previousInsurerViewModel: PreviousInsurerViewModel by sharedViewModel()

    private val insurerData by lazy {
        requireArguments()
            .getParcelable<PreviousInsurerParameter>(DATA)
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
            currentInsurerContainer.setHapticClickListener {
                onShowInsurers()
            }
            continueButton.setHapticClickListener {
                onContinue()
            }

            previousInsurerViewModel.previousInsurer.observe(viewLifecycleOwner) { selectedInsurer ->
                continueButton.isEnabled = selectedInsurer != null
                if (selectedInsurer?.isNotEmpty() == true) {
                    currentInsurerLabel.text = selectedInsurer
                }
            }

            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }
        }
    }

    private fun onContinue() {
        previousInsurerViewModel.previousInsurer.value?.let { id ->
            model.putInStore(insurerData.storeKey, id)
            model.navigateToPassage(insurerData.next)
        }
    }

    private fun onShowInsurers() {
        val fragment = PreviousInsurerBottomSheet.newInstance(insurerData.previousInsurers)
        fragment.show(parentFragmentManager, PreviousInsurerBottomSheet.TAG)
    }

    companion object {
        private const val DATA = "DATA"

        fun newInstance(previousInsurerData: PreviousInsurerParameter) =
            PreviousInsurerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, previousInsurerData)
                }
            }
    }
}
