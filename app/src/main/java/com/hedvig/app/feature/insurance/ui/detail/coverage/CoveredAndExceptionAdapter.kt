package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.CoveredAndExceptionHeaderBinding
import com.hedvig.app.databinding.CoveredAndExceptionItemBinding
import com.hedvig.app.databinding.PerilDescriptionBinding
import com.hedvig.app.databinding.PerilIconBinding
import com.hedvig.app.databinding.PerilMoreInfoBinding
import com.hedvig.app.databinding.PerilParagraphBinding
import com.hedvig.app.databinding.PerilTitleBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.svg.buildRequestBuilder

class CoveredAndExceptionAdapter :
    ListAdapter<CoveredAndExceptionModel, CoveredAndExceptionAdapter.ViewHolder>(
        GenericDiffUtilItemCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.covered_and_exception_item -> ViewHolder.CoveredAndExceptionViewHolder(parent)
        R.layout.covered_and_exception_header -> ViewHolder.HeaderViewHolder(parent)
        R.layout.peril_paragraph -> ViewHolder.ParagraphViewHolder(parent)
        R.layout.peril_icon -> ViewHolder.IconViewHolder(parent)
        R.layout.peril_title -> ViewHolder.TitleViewHolder(parent)
        R.layout.peril_description -> ViewHolder.DescriptionViewHolder(parent)
        R.layout.peril_more_info -> ViewHolder.MoreInfoViewHolder(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CoveredAndExceptionModel.Covered -> R.layout.covered_and_exception_item
        is CoveredAndExceptionModel.Exception -> R.layout.covered_and_exception_item
        CoveredAndExceptionModel.Header.CoveredHeader -> R.layout.covered_and_exception_header
        CoveredAndExceptionModel.Header.ExceptionHeader -> R.layout.covered_and_exception_header
        CoveredAndExceptionModel.Header.InfoHeader -> R.layout.covered_and_exception_header
        is CoveredAndExceptionModel.Paragraph -> R.layout.peril_paragraph
        is CoveredAndExceptionModel.Icon -> R.layout.peril_icon
        is CoveredAndExceptionModel.Title -> R.layout.peril_title
        is CoveredAndExceptionModel.Description -> R.layout.peril_description
        is CoveredAndExceptionModel.MoreInfo -> R.layout.peril_more_info
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: CoveredAndExceptionModel)

        class CoveredAndExceptionViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.covered_and_exception_item, parent, false)
        ) {
            private val binding by viewBinding(CoveredAndExceptionItemBinding::bind)
            override fun bind(item: CoveredAndExceptionModel) {
                if (item is CoveredAndExceptionModel.Exception || item is CoveredAndExceptionModel.Covered) {
                    binding.apply {
                        when (item) {
                            is CoveredAndExceptionModel.Covered -> {
                                icon.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        icon.context,
                                        R.drawable.ic_checkmark_in_circle
                                    )
                                )
                                text.text = item.text
                            }
                            is CoveredAndExceptionModel.Exception -> {
                                icon.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        icon.context,
                                        R.drawable.ic_terminated_colorless
                                    )
                                )
                                text.text = item.text
                            }
                        }
                    }
                } else return
            }
        }

        class HeaderViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.covered_and_exception_header, parent, false)
        ) {
            private val binding by viewBinding(CoveredAndExceptionHeaderBinding::bind)
            override fun bind(item: CoveredAndExceptionModel) {
                if (item !is CoveredAndExceptionModel.Header) {
                    return
                }
                binding.root.apply {
                    when (item) {
                        CoveredAndExceptionModel.Header.CoveredHeader -> {
                            setText(R.string.PERIL_MODAL_COVERAGE_TITLE)
                        }
                        CoveredAndExceptionModel.Header.ExceptionHeader -> {
                            setText(R.string.PERIL_MODAL_EXCEPTIONS_TITLE)
                        }
                        CoveredAndExceptionModel.Header.InfoHeader -> {
                            setText(R.string.PERIL_MODAL_INFO_TITLE)
                        }
                    }
                }
            }
        }

        class ParagraphViewHolder(parent: ViewGroup) :
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.peril_paragraph, parent, false)
            ) {
            private val binding by viewBinding(PerilParagraphBinding::bind)
            override fun bind(item: CoveredAndExceptionModel) {
                if (item !is CoveredAndExceptionModel.Paragraph) {
                    return
                }
                binding.root.text = item.text
            }
        }

        class IconViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.peril_icon, parent, false)
        ) {
            private val binding by viewBinding(PerilIconBinding::bind)
            override fun bind(item: CoveredAndExceptionModel) {
                if (item !is CoveredAndExceptionModel.Icon) {
                    return
                }
                binding.root.apply {
                    context.buildRequestBuilder()
                        .load(item.link)
                        .into(this)
                }
            }
        }

        class TitleViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.peril_title, parent, false)
        ) {
            private val binding by viewBinding(PerilTitleBinding::bind)
            override fun bind(item: CoveredAndExceptionModel) {
                if (item !is CoveredAndExceptionModel.Title) {
                    return
                }
                binding.root.text = item.text
            }
        }

        class DescriptionViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.peril_description, parent, false)
        ) {
            private val binding by viewBinding(PerilDescriptionBinding::bind)
            override fun bind(item: CoveredAndExceptionModel) {
                if (item !is CoveredAndExceptionModel.Description) {
                    return
                }
                binding.root.text = item.text
            }
        }

        class MoreInfoViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.peril_more_info, parent, false)
        ) {
            private val binding by viewBinding(PerilMoreInfoBinding::bind)
            override fun bind(item: CoveredAndExceptionModel) {
                if (item !is CoveredAndExceptionModel.MoreInfo) {
                    return
                }
                binding.root.setHapticClickListener { item.action() }
            }
        }
    }
}

sealed class CoveredAndExceptionModel {
    data class Icon(val link: String) : CoveredAndExceptionModel()
    data class Title(val text: String) : CoveredAndExceptionModel()
    data class Description(val text: String) : CoveredAndExceptionModel()
    data class MoreInfo(val action: () -> Unit) : CoveredAndExceptionModel()
    data class Covered(val text: String) : CoveredAndExceptionModel()
    data class Exception(val text: String) : CoveredAndExceptionModel()
    sealed class Header : CoveredAndExceptionModel() {
        object CoveredHeader : Header()
        object ExceptionHeader : Header()
        object InfoHeader : Header()
    }

    data class Paragraph(val text: String) : CoveredAndExceptionModel()
}
