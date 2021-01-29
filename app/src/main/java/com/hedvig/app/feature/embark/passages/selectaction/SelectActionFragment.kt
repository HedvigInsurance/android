package com.hedvig.app.feature.embark.passages.selectaction

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkSelectActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.passages.animateResponse
import com.hedvig.app.util.extensions.view.hapticClicks
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SelectActionFragment : Fragment(R.layout.fragment_embark_select_action) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentEmbarkSelectActionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = requireArguments().getParcelable<SelectActionParameter>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        binding.apply {
            messages.adapter = MessageAdapter(data.messages)
            actions.adapter = SelectActionAdapter { selectAction: SelectActionParameter.SelectAction, view: View ->
                view.hapticClicks()
                    .mapLatest { onActionSelected(selectAction, data, response) }
                    .onEach { model.navigateToPassage(selectAction.link) }
                    .launchIn(lifecycleScope)
            }.apply {
                submitList(data.actions)
            }
            actions.addItemDecoration(SelectActionDecoration())
        }
    }

    private suspend fun onActionSelected(selectAction: SelectActionParameter.SelectAction, data: SelectActionParameter, response: TextView) {
        selectAction.keys.zip(selectAction.values).forEach { (key, value) ->
            model.putInStore(key, value)
        }
        model.putInStore("${data.passageName}Result", selectAction.label)
        val responseText = model.preProcessResponse(data.passageName) ?: selectAction.label
        animateResponse(response, responseText)
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
