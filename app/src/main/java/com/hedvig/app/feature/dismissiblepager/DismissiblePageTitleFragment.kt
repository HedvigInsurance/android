package com.hedvig.app.feature.dismissiblepager

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.DismissiblePageTitleFragmentBinding
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.svg.buildRequestBuilder
import e

class DismissiblePageTitleFragment : Fragment(R.layout.dismissible_page_title_fragment) {
    private val requestBuilder: RequestBuilder<PictureDrawable> by lazy { buildRequestBuilder() }
    private val binding by viewBinding(DismissiblePageTitleFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model = requireArguments().getParcelable<DismissiblePagerModel.TitlePage>("model")

        if (model == null) {
            e { "Programmer error: incorrect arguments passed to ${this.javaClass.name}" }
            return
        }

        binding.apply {
            requestBuilder
                .load(Uri.parse(BuildConfig.BASE_URL + model.imageUrls.iconByTheme(requireContext())))
                .into(illustration)
            title.text = model.title
            paragraph.text = model.paragraph
        }
    }

    companion object {
        fun newInstance(
            model: DismissiblePagerModel.TitlePage
        ) = DismissiblePageTitleFragment().also {
            it.arguments = bundleOf(
                "model" to model
            )
        }
    }
}
