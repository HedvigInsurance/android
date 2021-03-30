package com.hedvig.app.feature.embark.passages.textactionset

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkInputItemBinding
import com.hedvig.app.feature.embark.passages.textaction.TextFieldData
import com.hedvig.app.feature.embark.setInputType
import com.hedvig.app.feature.embark.setValidationFormatter
import com.hedvig.app.feature.embark.validationCheck
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.onImeAction
import com.hedvig.app.util.extensions.viewBinding


class TextInputSetAdapter(val model: TextActionSetViewModel, private val onDone: () -> Unit) :
    ListAdapter<TextFieldData, TextInputSetAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position, model, onDone)
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.embark_input_item)) {
        private val binding by viewBinding(EmbarkInputItemBinding::bind)
        fun bind(item: TextFieldData, position: Int, model: TextActionSetViewModel, onDone: () -> Unit) {
            binding.apply {
                textField.hint = item.hint
                textField.placeholderText = item.placeholder
                input.onImeAction {
                    if (model.isValid.value == true) {
                        onDone()
                    }
                }

                item.mask?.let { mask ->
                    input.apply {
                        setInputType(mask)
                        setValidationFormatter(item.mask)
                    }
                }

                input.onChange { text ->
                    if (item.mask == null) {
                        if (text.isBlank()) {
                            model.updateIsValid(position, false)
                        } else {
                            model.updateIsValid(position, true)
                        }
                    } else {
                        if (text.isNotBlank() && validationCheck(item.mask, text)) {
                            model.updateIsValid(position, true)
                        } else {
                            model.updateIsValid(position, false)
                        }
                    }
                    model.setInputValue(position, text)
                }
                item.prefill?.let { input.setText(it) }
            }
        }
    }
}

