package com.hedvig.app.feature.embark.passages.selectaction

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkResponseBinding
import com.hedvig.app.databinding.FragmentEmbarkSelectActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.Response
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.feature.embark.passages.selectaction.ui.SelectActionView
import com.hedvig.app.feature.embark.ui.EmbarkActivity.Companion.PASSAGE_ANIMATION_DELAY_MILLIS
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.hedvig.app.util.whenApiVersion
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
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
                actionsComposeView.setupInsetsForIme(root, actionsComposeView)
            }

            if (data.actions.size == 1) {
                bindSingleButton(data.actions.first(), data)
            } else {
                bindAdapter(data)
            }

            messages.adapter = MessageAdapter(data.messages)
            messages.doOnNextLayout {
                startPostponedEnterTransition()
            }
        }
    }

    private fun FragmentEmbarkSelectActionBinding.bindSingleButton(
        action: SelectActionParameter.SelectAction,
        data: SelectActionParameter,
    ) {
        with(singleActionButton) {
            isVisible = true
            hapticClicks()
                .mapLatest { onActionSelected(action, data, responseContainer) }
                .onEach { model.submitAction(action.link, 0) }
                .launchIn(viewLifecycleScope)
            text = action.label
        }
    }

    private fun FragmentEmbarkSelectActionBinding.bindAdapter(data: SelectActionParameter) {
        with(actionsComposeView) {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var actionJob: Job? = null
                HedvigTheme {
                    SelectActionView(
                        selectActions = data.actions,
                        onActionClick = { selectAction: SelectActionParameter.SelectAction, position: Int ->
                            actionJob?.cancel()
                            actionJob = viewLifecycleScope.launch {
                                onActionSelected(selectAction, data, responseContainer)
                                yield()
                                model.submitAction(selectAction.link, position)
                            }
                        }
                    )
                }
            }
        }
    }

    private suspend fun onActionSelected(
        selectAction: SelectActionParameter.SelectAction,
        data: SelectActionParameter,
        responseBinding: EmbarkResponseBinding,
    ) {
        (selectAction.keys zip selectAction.values).forEach { (key, value) ->
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
