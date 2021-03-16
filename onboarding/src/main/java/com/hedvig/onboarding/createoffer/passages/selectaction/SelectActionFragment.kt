package com.hedvig.onboarding.createoffer.passages.selectaction

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
import com.hedvig.onboarding.R
import com.hedvig.onboarding.createoffer.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_MILLIS
import com.hedvig.onboarding.createoffer.EmbarkViewModel
import com.hedvig.onboarding.createoffer.passages.MessageAdapter
import com.hedvig.onboarding.createoffer.passages.animateResponse
import com.hedvig.onboarding.databinding.FragmentEmbarkSelectActionBinding
import e
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.android.viewmodel.ext.android.sharedViewModel

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
                    .mapLatest { onActionSelected(selectAction, data, response) }
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
        response: TextView,
    ) {
        selectAction.keys.zip(selectAction.values).forEach { (key, value) ->
            model.putInStore(key, value)
        }
        model.putInStore("${data.passageName}Result", selectAction.label)
        val responseText = model.preProcessResponse(data.passageName) ?: selectAction.label
        animateResponse(response, responseText)
        delay(PASSAGE_ANIMATION_DELAY_MILLIS)
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: SelectActionParameter) =
            SelectActionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, data)
                }
            }
    }
}
