package com.hedvig.app.feature.dismissiblepager

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentNewsBinding
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.svg.buildRequestBuilder
import androidx.fragment.app.Fragment

class DismissiblePageFragment : Fragment() {
    private val requestBuilder: RequestBuilder<PictureDrawable> by lazy { buildRequestBuilder() }
    private val binding by viewBinding(FragmentNewsBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_news, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            arguments?.getString(ILLUSTRATION)?.let { il ->
                requestBuilder
                    .load(Uri.parse(BuildConfig.BASE_URL + il))
                    .into(illustration)
            }
            title.text = arguments?.getString(TITLE)
            paragraph.text = arguments?.getString(PARAGRAPH)
        }
    }

    companion object {
        fun newInstance(
            illustration: String?,
            title: String?,
            paragraph: String
        ): DismissiblePageFragment {
            val fragment = DismissiblePageFragment()

            val arguments = Bundle().apply {
                putString(ILLUSTRATION, illustration)
                putString(TITLE, title)
                putString(PARAGRAPH, paragraph)
            }
            fragment.arguments = arguments

            return fragment
        }

        private const val ILLUSTRATION = "ILLUSTRATION"
        private const val TITLE = "TITLE"
        private const val PARAGRAPH = "PARAGRAPH"
    }
}
