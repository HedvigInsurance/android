package com.hedvig.app.feature.embark.passages.selectaction

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkResponseBinding
import com.hedvig.app.databinding.FragmentEmbarkSelectActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.Response
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_MILLIS
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectActionFragment : Fragment(R.layout.fragment_embark_select_action) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentEmbarkSelectActionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        val data = requireArguments().getParcelable<SelectActionParameter>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        binding.apply {
            whenApiVersion(Build.VERSION_CODES.R) {
                actions.setupInsetsForIme(
                    root = root,
                    actions,
                )
            }

            messages.adapter = MessageAdapter(data.messages)
            actions.adapter = SelectActionAdapter { selectAction: SelectActionParameter.SelectAction, view: View ->
                view.hapticClicks()
                    .mapLatest { onActionSelected(selectAction, data, responseContainer) }
                    .onEach { model.navigateToPassage(selectAction.link) }
                    .launchIn(viewLifecycleScope)
            }.apply {
                submitList(data.actions)
            }
            actions.addItemDecoration(SelectActionDecoration())

            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }
        }
    }

    private suspend fun onActionSelected(
        selectAction: SelectActionParameter.SelectAction,
        data: SelectActionParameter,
        responseBinding: EmbarkResponseBinding,
    ) {
        selectAction.keys.zip(selectAction.values).forEach { (key, value) ->
            model.putInStore(key, value)
        }
        model.putInStore("${data.passageName}Result", selectAction.label)
        val response =
            model.preProcessResponse(data.passageName) ?: Response.SingleResponse(selectAction.label)
        animateResponse(responseBinding, response)
        delay(PASSAGE_ANIMATION_DELAY_MILLIS)
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: SelectActionParameter) =
            SelectActionFragment().apply {
                arguments = bundleOf(
                    DATA to data
                )
            }
    }
}
