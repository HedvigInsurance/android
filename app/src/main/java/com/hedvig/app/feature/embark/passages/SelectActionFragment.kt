package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkSelectActionBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SelectActionFragment : Fragment(R.layout.fragment_embark_select_action) {
    private val model: EmbarkViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentEmbarkSelectActionBinding::bind)

    private var job: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = requireArguments().getParcelable<SelectActionPassage>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        binding.apply {
            messages.adapter = MessageAdapter().apply {
                submitList(data.messages)
            }
            actions.adapter = SelectActionAdapter { selectAction ->
                selectAction.keys.zip(selectAction.values).forEach { (key, value) ->
                    model.putInStore(key, value)
                }
                model.putInStore("${data.passageName}Result", selectAction.label)
                val responseText = model.preProcessResponse(data.passageName) ?: selectAction.label
                job?.cancel()
                job = lifecycleScope.launch {
                    animateResponse(response, responseText)
                    model.navigateToPassage(selectAction.link)
                }
            }.apply {
                submitList(data.actions)
            }
            actions.addItemDecoration(SelectActionDecoration())
        }
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: SelectActionPassage) =
            SelectActionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, data)
                }
            }
    }
}

@Parcelize
data class SelectActionPassage(
    val messages: List<String>,
    val actions: List<SelectAction>,
    val passageName: String
) : Parcelable {
    companion object {
        fun from(messages: List<String>, data: EmbarkStoryQuery.Data1, passageName: String) =
            SelectActionPassage(
                messages,
                data.options.map {
                    SelectAction(
                        it.link.fragments.embarkLinkFragment.name,
                        it.link.fragments.embarkLinkFragment.label,
                        it.keys,
                        it.values
                    )
                },
                passageName
            )
    }
}

@Parcelize
data class SelectAction(
    val link: String,
    val label: String,
    val keys: List<String>,
    val values: List<String>
) : Parcelable
