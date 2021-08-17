package com.hedvig.app.feature.embark.passages.previousinsurer

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.app.R
import com.hedvig.app.databinding.PreviousInsurerFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.previousinsurer.askforprice.AskForPriceInfoActivity
import com.hedvig.app.feature.embark.passages.previousinsurer.askforprice.AskForPriceInfoParameter
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureFlagProvider
import com.hedvig.app.util.whenApiVersion
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PreviousInsurerFragment : Fragment(R.layout.previous_insurer_fragment) {

    private val binding by viewBinding(PreviousInsurerFragmentBinding::bind)
    private val model: EmbarkViewModel by sharedViewModel()
    private val previousInsurerViewModel: PreviousInsurerViewModel by sharedViewModel()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                onContinue()
            }
        }
    private val featureFlagProvider: FeatureFlagProvider by inject()

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
                if (featureFlagProvider.hasFeature(Feature.INSURELY_EMBARK)) {
                    previousInsurerViewModel.previousInsurer.value?.name?.let {
                        startActivity(
                            AskForPriceInfoActivity.createIntent(
                                requireContext(),
                                AskForPriceInfoParameter(it)
                            )
                        )
                    }
                } else {
                    onContinue()
                }
            }

            previousInsurerViewModel.previousInsurer.observe(viewLifecycleOwner) { selectedInsurer ->
                continueButton.isEnabled = selectedInsurer != null
                if (selectedInsurer?.name?.isNotEmpty() == true) {
                    currentInsurerLabel.text = selectedInsurer.name
                }
            }

            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }
        }
    }

    private fun startAskForPrice() {
        previousInsurerViewModel.previousInsurer.value?.name?.let {
            startForResult.launch(
                AskForPriceInfoActivity.createIntent(
                    requireContext(),
                    AskForPriceInfoParameter(it)
                )
            )
        }
    }

    private fun onContinue() {
        previousInsurerViewModel.previousInsurer.value?.let { item ->
            if (item.id == getString(R.string.EXTERNAL_INSURANCE_PROVIDER_OTHER_OPTION)) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.EXTERNAL_INSURANCE_PROVIDER_ALERT_TITLE))
                    .setMessage(getString(R.string.EXTERNAL_INSURANCE_PROVIDER_ALERT_MESSAGE))
                    .setPositiveButton(getString(R.string.ALERT_OK)) { dialog, _ -> dialog.dismiss() }
                    .show()
            } else {
                model.putInStore(insurerData.storeKey, item.id)
                model.submitAction(insurerData.next)
            }
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
