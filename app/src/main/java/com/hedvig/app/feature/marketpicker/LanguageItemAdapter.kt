package com.hedvig.app.feature.marketpicker

import android.app.Dialog
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerItemLayoutBinding
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class LanguageItemAdapter(
    private val viewModel: MarketPickerViewModel,
    private val tracker: MarketPickerTracker,
    private val dialog: Dialog?
) : ListAdapter<Language, LanguageItemAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel, tracker, dialog)
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        parent.inflate(R.layout.picker_item_layout)
    ) {
        val binding by viewBinding(PickerItemLayoutBinding::bind)
        fun bind(
            language: Language,
            viewModel: MarketPickerViewModel,
            tracker: MarketPickerTracker,
            dialog: Dialog?
        ) {
            binding.apply {
                radioButton.isChecked = viewModel.data.value?.language == language
                text.text = text.context.getString(language.getLabel())

                root.setHapticClickListener {
                    tracker.selectLocale(language)
                    viewModel.submitLanguageAndReload(market = null, language = language)
                    dialog?.cancel()
                }
            }
        }
    }
}
