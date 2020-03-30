package com.hedvig.app.feature.offer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.svg.buildRequestBuilder
import e
import kotlinx.android.synthetic.main.peril_bottom_sheet_new.*

class PerilBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.peril_bottom_sheet_new, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleText = requireArguments().getString(TITLE)
        val bodyText = requireArguments().getString(BODY)
        val iconUrl = requireArguments().getString(ICON_URL)

        if (titleText == null || bodyText == null || iconUrl == null) {
            e { "Programmer error: Missing either TITLE, BODY or ICON_URL in ${this.javaClass.name}" }
            return
        }

        val requestBuilder = buildRequestBuilder()

        requestBuilder
            .load(iconUrl)
            .into(icon)
        title.text = titleText
        body.text = bodyText
    }

    companion object {
        private const val TITLE = "TITLE"
        private const val BODY = "BODY"
        private const val ICON_URL = "ICON_URL"

        val TAG = PerilBottomSheet::class.java.name

        fun newInstance(context: Context, peril: OfferQuery.Peril) = PerilBottomSheet().apply {
            arguments = Bundle().apply {
                putString(TITLE, peril.title)
                putString(BODY, peril.description)
                putString(
                    ICON_URL, "${BuildConfig.BASE_URL}${if (context.isDarkThemeActive) {
                        peril.icon.variants.dark.svgUrl
                    } else {
                        peril.icon.variants.light.svgUrl
                    }}"
                )
            }
        }
    }
}
