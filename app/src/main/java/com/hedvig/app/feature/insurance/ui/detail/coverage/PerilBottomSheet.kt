package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import com.bumptech.glide.RequestBuilder
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.R
import com.hedvig.app.ui.view.ExpandableBottomSheet
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.isDarkThemeActive
import e
import org.koin.android.ext.android.inject

class PerilBottomSheet : ExpandableBottomSheet() {
    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val peril = requireArguments().getParcelable<Peril>(PERIL)

        if (peril == null) {
            e { "Programmer error: Missing arguments in ${this@PerilBottomSheet.javaClass.name}" }
            return
        }

        binding.recycler.updatePadding(bottom = binding.recycler.paddingBottom + 56.dp)
        binding.recycler.adapter = PerilAdapter(requestBuilder).also { adapter ->
            adapter.submitList(
                expandedList(
                    peril.title,
                    peril.description,
                    peril.info,
                    peril.covered,
                    peril.exception,
                    peril.iconUrl
                )
            )
        }
    }

    private fun expandedList(
        title: String,
        description: String,
        info: String,
        covered: List<String>,
        exceptions: List<String>,
        iconLink: String,
    ) = listOfNotNull(
        PerilModel.Icon(iconLink),
        PerilModel.Title(title),
        PerilModel.Description(description),
        if (covered.isNotEmpty()) {
            PerilModel.Header.CoveredHeader
        } else {
            null
        },
        *covered.map { PerilModel.PerilList.Covered(it) }.toTypedArray(),
        if (exceptions.isNotEmpty()) {
            PerilModel.Header.ExceptionHeader
        } else {
            null
        },
        *exceptions.map { PerilModel.PerilList.Exception(it) }.toTypedArray(),
        *(
            if (info.isNotBlank()) {
                arrayOf(PerilModel.Header.InfoHeader, PerilModel.Paragraph(info))
            } else {
                emptyArray()
            }
            )

    )

    companion object {
        private const val PERIL = "PERIL"

        val TAG = PerilBottomSheet::class.java.name

        fun newInstance(context: Context, peril: PerilFragment) = PerilBottomSheet().apply {
            arguments = bundleOf(
                PERIL to Peril(
                    title = peril.title,
                    description = peril.description,
                    iconUrl = "${context.getString(R.string.BASE_URL)}${
                        if (context.isDarkThemeActive) {
                            peril.icon.variants.dark.svgUrl
                        } else {
                            peril.icon.variants.light.svgUrl
                        }
                    }",
                    exception = peril.exceptions,
                    covered = peril.covered,
                    info = peril.info
                )
            )
        }
    }
}
