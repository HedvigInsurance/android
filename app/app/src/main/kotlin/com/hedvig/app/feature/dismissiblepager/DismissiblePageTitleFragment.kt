package com.hedvig.app.feature.dismissiblepager

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import coil.ImageLoader
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.app.R
import com.hedvig.app.databinding.DismissiblePageTitleFragmentBinding
import com.hedvig.app.ui.coil.load
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject

class DismissiblePageTitleFragment : Fragment(R.layout.dismissible_page_title_fragment) {
  private val imageLoader: ImageLoader by inject()
  private val binding by viewBinding(DismissiblePageTitleFragmentBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val model = requireArguments().parcelable<DismissiblePagerModel.TitlePage>("model")
      ?: error("Programmer error: incorrect arguments passed to ${this.javaClass.name}")

    binding.apply {
      val url =
        Uri.parse(
          requireContext().getString(hedvig.resources.R.string.BASE_URL) + model.imageUrls.iconByTheme(requireContext()),
        )
      illustration.load(url, imageLoader)
      title.text = model.title
      paragraph.text = model.paragraph
    }
  }

  companion object {
    fun newInstance(
      model: DismissiblePagerModel.TitlePage,
    ) = DismissiblePageTitleFragment().also {
      it.arguments = bundleOf(
        "model" to model,
      )
    }
  }
}
