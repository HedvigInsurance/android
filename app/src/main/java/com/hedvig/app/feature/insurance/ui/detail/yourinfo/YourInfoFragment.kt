package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailYourInfoFragmentBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.android.viewmodel.ext.android.sharedViewModel

class YourInfoFragment : Fragment(R.layout.contract_detail_your_info_fragment) {
    private val binding by viewBinding(ContractDetailYourInfoFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.root.apply {
            doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            adapter = YourInfoAdapter()

            model.data.observe(viewLifecycleOwner) { data ->
                data.currentAgreement.asSwedishApartmentAgreement?.let {
                    (adapter as? YourInfoAdapter)?.submitList(
                        homeSection(
                            it.address.fragments.addressFragment,
                            it.squareMeters,
                            it.saType.displayName(requireContext())
                        ) + coinsuredSection(it.numberCoInsured)
                    )
                    return@observe
                }
                data.currentAgreement.asSwedishHouseAgreement?.let {
                    (adapter as? YourInfoAdapter)?.submitList(
                        homeSection(
                            it.address.fragments.addressFragment,
                            it.squareMeters,
                            getString(R.string.SWEDISH_HOUSE_LOB)
                        ) + coinsuredSection(it.numberCoInsured)
                    )
                    return@observe
                }
                data.currentAgreement.asNorwegianHomeContentAgreement?.let {
                    (adapter as? YourInfoAdapter)?.submitList(
                        homeSection(
                            it.address.fragments.addressFragment,
                            it.squareMeters,
                            it.nhcType?.displayName(requireContext()) ?: ""
                        ) + coinsuredSection(it.numberCoInsured)
                    )
                    return@observe
                }
                data.currentAgreement.asNorwegianTravelAgreement?.let {
                    (adapter as? YourInfoAdapter)?.submitList(
                        coinsuredSection(it.numberCoInsured)
                    )
                    return@observe
                }
            }
        }
    }

    private fun homeSection(address: AddressFragment, sqm: Int, typeTranslated: String) = listOf(
        YourInfoModel.Header.Details,
        YourInfoModel.Row(
            getString(R.string.CONTRACT_DETAIL_HOME_ADDRESS),
            address.street,
        ),
        YourInfoModel.Row(
            getString(R.string.CONTRACT_DETAIL_HOME_POSTCODE),
            address.postalCode,
        ),
        YourInfoModel.Row(
            getString(R.string.CONTRACT_DETAIL_HOME_SIZE),
            getString(R.string.CONTRACT_DETAIL_HOME_SIZE_INPUT, sqm),
        ),
        YourInfoModel.Row(
            getString(R.string.CONTRACT_DETAIL_HOME_TYPE),
            typeTranslated,
        )
    )

    private fun coinsuredSection(amount: Int) = listOf(
        YourInfoModel.Header.Coinsured,
        YourInfoModel.Row(
            getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER),
            when (amount) {
                0 -> getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT_ZERO_COINSURED)
                1 -> getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT_ONE_COINSURED)
                else -> getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT, amount)
            }
        )
    )

    companion object {

        internal fun SwedishApartmentLineOfBusiness.displayName(context: Context) = when (this) {
            SwedishApartmentLineOfBusiness.RENT -> context.getString(R.string.SWEDISH_APARTMENT_LOB_RENT)
            SwedishApartmentLineOfBusiness.BRF -> context.getString(R.string.SWEDISH_APARTMENT_LOB_BRF)
            SwedishApartmentLineOfBusiness.STUDENT_RENT -> context.getString(R.string.SWEDISH_APARTMENT_LOB_STUDENT_RENT)
            SwedishApartmentLineOfBusiness.STUDENT_BRF -> context.getString(R.string.SWEDISH_APARTMENT_LOB_STUDENT_BRF)
            SwedishApartmentLineOfBusiness.UNKNOWN__ -> ""
        }

        internal fun NorwegianHomeContentLineOfBusiness.displayName(context: Context) =
            when (this) {
                NorwegianHomeContentLineOfBusiness.RENT -> context.getString(R.string.NORWEIGIAN_HOME_CONTENT_LOB_RENT)
                NorwegianHomeContentLineOfBusiness.OWN -> context.getString(R.string.NORWEIGIAN_HOME_CONTENT_LOB_OWN)
                NorwegianHomeContentLineOfBusiness.YOUTH_RENT -> context.getString(R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_RENT)
                NorwegianHomeContentLineOfBusiness.YOUTH_OWN -> context.getString(R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_OWN)
                NorwegianHomeContentLineOfBusiness.UNKNOWN__ -> ""
            }
    }
}
