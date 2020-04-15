package com.hedvig.app.feature.keygear

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.svg.buildRequestBuilder
import e
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.dialog_key_gear_coverage.*

class KeyGearCoverageBottomSheet : RoundedBottomSheetDialogFragment() {

    private val requestBuilder by lazy { buildRequestBuilder() }

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.dialog_key_gear_coverage)

        arguments?.getParcelable<KeyGearItemCoverage>(DATA)?.let { data ->
            requestBuilder.load(data.icon.iconByTheme(requireContext()))
                .into(dialog.icon)

            dialog.title.text = data.title
            dialog.description.text = data.description

            dialog.bulletPointsRecyclerView.adapter =
                CoverageInfoAdapter(data.boxes, requestBuilder)
        } ?: e { "No data provided" }

        return dialog
    }

    companion object {
        const val TAG = "coverageBottomSheet"

        private const val DATA = "DATA"

        fun newInstance(data: KeyGearItemCoverage) = KeyGearCoverageBottomSheet().apply {
            arguments = Bundle().also { b ->
                b.putParcelable(DATA, data)
            }
        }
    }
}

@Parcelize
data class KeyGearItemCoverage(
    val id: String,
    val icon: ThemedIconUrls,
    val title: String,
    val description: String,
    val boxes: List<KeyGearItemCoverageBox>
) : Parcelable

@Parcelize
data class KeyGearItemCoverageBox(
    val id: String,
    val icon: ThemedIconUrls,
    val title: String,
    val description: String
) : Parcelable

val mock = KeyGearItemCoverage(
    "123",
    ThemedIconUrls(
        "https://image.flaticon.com/icons/svg/2210/2210794.svg",
        "https://image.flaticon.com/icons/svg/2210/2210794.svg"
    ),
    "hallå",
    "Vi försöker reparera i första hand, men om din mobiltelefon skulle behöva ersättas helt (ex. om den blivit stulen) ersätts du med 70% av vad du köpte den för.",
    listOf(
        KeyGearItemCoverageBox(
            "123",
            ThemedIconUrls(
                "https://image.flaticon.com/icons/svg/2210/2210794.svg",
                "https://image.flaticon.com/icons/svg/2210/2210794.svg"
            ),
            "Ersättning",
            "Hedvig täcker hela kostnaden för en reparation minus självrisk (1 500 kr) eller det uppskattade värdet på telefonen minus självrisk om den inte går att reparera"
        ),
        KeyGearItemCoverageBox(
            "123",
            ThemedIconUrls(
                "https://image.flaticon.com/icons/svg/2210/2210794.svg",
                "https://image.flaticon.com/icons/svg/2210/2210794.svg"
            ),
            "Ersättning",
            "Hedvig täcker hela kostnaden för en reparation minus självrisk (1 500 kr) eller det uppskattade värdet på telefonen minus självrisk om den inte går att reparera"
        ),
        KeyGearItemCoverageBox(
            "123",
            ThemedIconUrls(
                "https://image.flaticon.com/icons/svg/2210/2210794.svg",
                "https://image.flaticon.com/icons/svg/2210/2210794.svg"
            ),
            "Ersättning",
            "Hedvig täcker hela kostnaden för en reparation minus självrisk (1 500 kr) eller det uppskattade värdet på telefonen minus självrisk om den inte går att reparera"
        ),
        KeyGearItemCoverageBox(
            "123",
            ThemedIconUrls(
                "https://image.flaticon.com/icons/svg/2210/2210794.svg",
                "https://image.flaticon.com/icons/svg/2210/2210794.svg"
            ),
            "Ersättning",
            "Hedvig täcker hela kostnaden för en reparation minus självrisk (1 500 kr) eller det uppskattade värdet på telefonen minus självrisk om den inte går att reparera"
        ),
        KeyGearItemCoverageBox(
            "123",
            ThemedIconUrls(
                "https://image.flaticon.com/icons/svg/2210/2210794.svg",
                "https://image.flaticon.com/icons/svg/2210/2210794.svg"
            ),
            "Ersättning",
            "Hedvig täcker hela kostnaden för en reparation minus självrisk (1 500 kr) eller det uppskattade värdet på telefonen minus självrisk om den inte går att reparera"
        )
    )
)
