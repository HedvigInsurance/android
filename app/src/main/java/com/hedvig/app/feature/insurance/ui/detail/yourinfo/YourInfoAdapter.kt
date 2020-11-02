package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailRowBinding
import com.hedvig.app.databinding.ContractDetailYourInfoHeaderBinding
import com.hedvig.app.databinding.YourInfoChatButtonBinding
import com.hedvig.app.databinding.YourInfoParagraphBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class YourInfoAdapter :
    ListAdapter<YourInfoModel, YourInfoAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is YourInfoModel.Header -> R.layout.contract_detail_your_info_header
        is YourInfoModel.Paragraph -> R.layout.your_info_paragraph
        YourInfoModel.ChangeParagraph -> R.layout.change_paragraph
        YourInfoModel.OpenChatButton -> R.layout.your_info_chat_button
        is YourInfoModel.Row -> R.layout.contract_detail_row
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.contract_detail_your_info_header -> ViewHolder.Header(parent)
        R.layout.your_info_paragraph -> ViewHolder.Paragraph(parent)
        R.layout.your_info_chat_button -> ViewHolder.Button(parent)
        R.layout.contract_detail_row -> ViewHolder.Row(parent)
        R.layout.change_paragraph -> ViewHolder.ChangeParagraph(parent)
        else -> throw Error("Invalid view type")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: YourInfoModel): Any?

        fun invalid(data: YourInfoModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Header(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.contract_detail_your_info_header)) {
            private val binding by viewBinding(ContractDetailYourInfoHeaderBinding::bind)
            override fun bind(data: YourInfoModel) = with(binding) {
                if (data !is YourInfoModel.Header) {
                    return invalid(data)
                }
                root.setText(
                    when (data) {
                        YourInfoModel.Header.Details -> R.string.CONTRACT_DETAIL_HOME_TITLE
                        YourInfoModel.Header.Coinsured -> R.string.CONTRACT_DETAIL_COINSURED_TITLE
                        YourInfoModel.Header.Change -> R.string.insurance_details_view_your_info_edit_insurance_title
                    }
                )
            }
        }

        class Row(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.contract_detail_row)) {
            private val binding by viewBinding(ContractDetailRowBinding::bind)
            override fun bind(data: YourInfoModel) = with(binding) {
                if (data !is YourInfoModel.Row) {
                    return invalid(data)
                }
                label.text = data.label
                content.text = data.content
            }
        }

        class Paragraph(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.your_info_paragraph)) {
            private val binding by viewBinding(YourInfoParagraphBinding::bind)
            override fun bind(data: YourInfoModel) = with(binding) {
                if (data !is YourInfoModel.Paragraph) {
                    return invalid(data)
                }
                root.text = data.text
            }
        }

        class ChangeParagraph(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.change_paragraph)) {
            override fun bind(data: YourInfoModel) {
            }
        }

        class Button(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.your_info_chat_button)) {
            private val binding by viewBinding(YourInfoChatButtonBinding::bind)
            override fun bind(data: YourInfoModel) = with(binding) {
                binding.root.apply {
                    setHapticClickListener {
                        context.startActivity(ChatActivity.newInstance(context, true))
                    }
                }
            }
        }
    }
}
