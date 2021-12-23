package com.hedvig.app.feature.dismissiblepager

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import coil.ImageLoader
import coil.load
import com.hedvig.app.R
import com.hedvig.app.databinding.DismissiblePageNoTitleFragmentBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e
import org.koin.android.ext.android.inject

class DismissiblePageNoTitleFragment : Fragment(R.layout.dismissible_page_no_title_fragment) {
    private val imageLoader: ImageLoader by inject()
    private val binding by viewBinding(DismissiblePageNoTitleFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model = requireArguments().getParcelable<DismissiblePagerModel.NoTitlePage>("model")

        if (model == null) {
            e { "Programmer error: incorrect arguments passed to ${this.javaClass.name}" }
            return
        }

        binding.apply {
            val url = Uri.parse(model.imageUrls.iconByTheme(requireContext()))
            illustration.load(url, imageLoader)
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
