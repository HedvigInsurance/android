package com.hedvig.app.feature.marketpicker

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerItemLayoutBinding
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class LanguagePickerBottomSheetAdapter(private val viewModel: MarketPickerViewModel) :
    RecyclerView.Adapter<LanguagePickerBottomSheetAdapter.ViewHolder>() {

    var items: List<Language> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(
                GenericDiffUtilCallback(
                    field,
                    value
                )
            )
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], viewModel)
    }

    override fun getItemCount() = items.size

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        parent.inflate(R.layout.picker_item_layout)
    ) {
        val binding by viewBinding(PickerItemLayoutBinding::bind)
        fun bind(language: Language, viewModel: MarketPickerViewModel) {
            binding.apply {
                radioButton.isChecked = viewModel.data.value?.language == language
                text.text = text.context.getString(language.getLabel())

                root.setHapticClickListener {
                    viewModel.data.postValue(viewModel.data.value?.copy(language = language))
                }
            }
        }
    }
}
