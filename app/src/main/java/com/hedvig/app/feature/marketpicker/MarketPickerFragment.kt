package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.google.android.material.transition.MaterialContainerTransform
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentMarketPickerBinding
import com.hedvig.app.feature.language.LanguageAndMarketViewModel
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.market_item.view.*
import org.koin.android.viewmodel.ext.android.viewModel

class MarketPickerFragment : Fragment(R.layout.fragment_market_picker) {
    private val viewModel: LanguageAndMarketViewModel by viewModel()
    private val binding by viewBinding(FragmentMarketPickerBinding::bind)

    @SuppressLint("ApplySharedPref")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val marketSelectedFragment = MarketSelectedFragment()
        marketSelectedFragment.sharedElementEnterTransition = MaterialContainerTransform()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val newMarketPref = sharedPreferences.getString(SettingsActivity.SETTINGS_NEW_MARKET, null)

        binding.apply {
            continueButton.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            continueButton.setHapticClickListener {
                viewModel.loadGeo()
               /* parentFragmentManager.beginTransaction()
                    .addSharedElement(continueButton, "marketButton")
                    .replace(R.id.container, marketSelectedFragment)
                    .addToBackStack(null)
                    .commit()*/
            }

            picker.adapter = PickerAdapter()

            /*when (requireContext().getMarket()) {
                Market.SE -> {
                    flag.setImageDrawable(requireContext().compatDrawable(R.drawable.ic_flag_se))
                    selectedCountry.text = requireContext().getText(R.string.sweden)
                }
                Market.NO -> {
                    flag.setImageDrawable(requireContext().compatDrawable(R.drawable.ic_flag_no))
                    selectedCountry.text = requireContext().getText(R.string.norway)
                }
                null -> {
                }
            }*/

        }
        bind()
        if (newMarketPref != null) {
            val market = Market.valueOf(newMarketPref)
            viewModel.preselectedMarket.postValue(market.name)
            viewModel.updateMarket(market)
            sharedPreferences.edit()
                .putString(
                    SettingsActivity.SETTINGS_NEW_MARKET,
                    null
                )
                .commit()
        } else {
            viewModel.loadGeo()
        }
    }

    private fun bind() {
        viewModel.marketAndLanguages.observe(viewLifecycleOwner) {
            (binding.picker.adapter as PickerAdapter).items = it
        }
    }
}
