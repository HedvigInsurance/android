package com.hedvig.app.feature.dismissablepager

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.util.svg.buildRequestBuilder
import kotlinx.android.synthetic.main.fragment_news.*

class DismissablePageFragment : androidx.fragment.app.Fragment() {
    private val requestBuilder: RequestBuilder<PictureDrawable> by lazy { buildRequestBuilder() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_news, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(ILLUSTRATION)?.let { il ->
            requestBuilder
                .load(Uri.parse(BuildConfig.BASE_URL + il))
                .into(illustration)
        }
        title.text = arguments?.getString(TITLE)
        paragraph.text = arguments?.getString(PARAGRAPH)
    }

    companion object {
        fun newInstance(illustration: String, title: String, paragraph: String): DismissablePageFragment {
            val fragment = DismissablePageFragment()

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
