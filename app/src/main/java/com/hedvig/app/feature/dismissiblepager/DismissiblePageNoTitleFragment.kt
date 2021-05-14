package com.hedvig.app.feature.dismissiblepager

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.R
import com.hedvig.app.databinding.DismissiblePageNoTitleFragmentBinding
import com.hedvig.app.util.svg.buildRequestBuilder
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e

class DismissiblePageNoTitleFragment : Fragment(R.layout.dismissible_page_no_title_fragment) {
    private val requestBuilder: RequestBuilder<PictureDrawable> by lazy { buildRequestBuilder() }
    private val binding by viewBinding(DismissiblePageNoTitleFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model = requireArguments().getParcelable<DismissiblePagerModel.NoTitlePage>("model")

        if (model == null) {
            e { "Programmer error: incorrect arguments passed to ${this.javaClass.name}" }
            return
        }

        binding.apply {
            requestBuilder
                .load(
                    Uri.parse(
                        requireContext().getString(R.string.BASE_URL)
                            + model.imageUrls.iconByTheme(requireContext())
                    )
                )
                .into(illustration)
            paragraph.text = model.paragraph
        }
    }

    companion object {
        fun newInstance(
            model: DismissiblePagerModel.NoTitlePage,
        ) = DismissiblePageNoTitleFragment().also {
            it.arguments = bundleOf(
                "model" to model
            )
        }
    }
}
