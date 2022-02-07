package com.hedvig.app.feature.embark.passages.noaction

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkNoActionBinding
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e

class NoActionFragment : Fragment(R.layout.fragment_embark_no_action) {
    private val binding by viewBinding(FragmentEmbarkNoActionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        val data = requireArguments().getParcelable<NoActionParameter>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        binding.apply {
            messages.adapter = MessageAdapter(data.messages)
            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }
        }
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: NoActionParameter) =
            NoActionFragment().apply {
                arguments = bundleOf(
                    DATA to data
                )
            }
    }
}
