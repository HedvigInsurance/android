package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.graphics.drawable.PictureDrawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.R
import com.hedvig.app.databinding.CoveredAndExceptionHeaderBinding
import com.hedvig.app.databinding.CoveredAndExceptionItemBinding
import com.hedvig.app.databinding.PerilDescriptionBinding
import com.hedvig.app.databinding.PerilIconBinding
import com.hedvig.app.databinding.PerilParagraphBinding
import com.hedvig.app.databinding.PerilTitleBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding
import e

class PerilAdapter(
    private val requestBuilder: RequestBuilder<PictureDrawable>
) :
    ListAdapter<CoveredAndExceptionModel, PerilAdapter.ViewHolder>(
        GenericDiffUtilItemCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.covered_and_exception_item -> ViewHolder.CoveredAndException(parent)
        R.layout.covered_and_exception_header -> ViewHolder.Header(parent)
        R.layout.peril_paragraph -> ViewHolder.Paragraph(parent)
        R.layout.peril_icon -> ViewHolder.Icon(parent)
        R.layout.peril_title -> ViewHolder.Title(parent)
        R.layout.peril_description -> ViewHolder.Description(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), requestBuilder)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        CoveredAndExceptionModel.Header.CoveredHeader -> R.layout.covered_and_exception_header
        CoveredAndExceptionModel.Header.ExceptionHeader -> R.layout.covered_and_exception_header
        CoveredAndExceptionModel.Header.InfoHeader -> R.layout.covered_and_exception_header
        is CoveredAndExceptionModel.Paragraph -> R.layout.peril_paragraph
        is CoveredAndExceptionModel.Icon -> R.layout.peril_icon
        is CoveredAndExceptionModel.Title -> R.layout.peril_title
        is CoveredAndExceptionModel.Description -> R.layout.peril_description
        is CoveredAndExceptionModel.CommonDenominator.Covered -> R.layout.covered_and_exception_item
        is CoveredAndExceptionModel.CommonDenominator.Exception -> R.layout.covered_and_exception_item
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            item: CoveredAndExceptionModel,
            requestBuilder: RequestBuilder<PictureDrawable>
        )

        fun invalid(data: CoveredAndExceptionModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class CoveredAndException(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.covered_and_exception_item)) {
            private val binding by viewBinding(CoveredAndExceptionItemBinding::bind)
            override fun bind(
                item: CoveredAndExceptionModel,
                requestBuilder: RequestBuilder<PictureDrawable>
            ) {
                if (item !is CoveredAndExceptionModel.CommonDenominator) {
                    invalid(item)
                    return
                }
                binding.apply {
                    when (item) {
                        is CoveredAndExceptionModel.CommonDenominator.Covered -> {
                            icon.setImageResource(R.drawable.ic_checkmark_in_circle)
                            text.text = item.text
                        }
                        is CoveredAndExceptionModel.CommonDenominator.Exception -> {
                            icon.setImageResource(R.drawable.ic_terminated_colorless)
                            text.text = item.text
                        }
                    }
                }
            }
        }

        class Header(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.covered_and_exception_header)) {
            private val binding by viewBinding(CoveredAndExceptionHeaderBinding::bind)
            override fun bind(
                item: CoveredAndExceptionModel,
                requestBuilder: RequestBuilder<PictureDrawable>
            ) {
                if (item !is CoveredAndExceptionModel.Header) {
                    invalid(item)
                    return
                }
                binding.root.apply {
                    setText(
                        when (item) {
                            CoveredAndExceptionModel.Header.CoveredHeader -> {
                                R.string.PERIL_MODAL_COVERAGE_TITLE
                            }
                            CoveredAndExceptionModel.Header.ExceptionHeader -> {
                                R.string.PERIL_MODAL_EXCEPTIONS_TITLE
                            }
                            CoveredAndExceptionModel.Header.InfoHeader -> {
                                R.string.PERIL_MODAL_INFO_TITLE
                            }
                        }
                    )
                }
            }
        }

        class Paragraph(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.peril_paragraph)) {
            private val binding by viewBinding(PerilParagraphBinding::bind)
            override fun bind(
                item: CoveredAndExceptionModel,
                requestBuilder: RequestBuilder<PictureDrawable>
            ) {
                if (item !is CoveredAndExceptionModel.Paragraph) {
                    invalid(item)
                    return
                }
                binding.root.text = item.text
            }
        }

        class Icon(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.peril_icon)) {
            private val binding by viewBinding(PerilIconBinding::bind)
            override fun bind(
                item: CoveredAndExceptionModel,
                requestBuilder: RequestBuilder<PictureDrawable>
            ) {
                if (item !is CoveredAndExceptionModel.Icon) {
                    invalid(item)
                    return
                }
                binding.root.apply {
                    requestBuilder.load(item.link).into(this)
                }
            }
        }

        class Title(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.peril_title)) {
            private val binding by viewBinding(PerilTitleBinding::bind)
            override fun bind(
                item: CoveredAndExceptionModel,
                requestBuilder: RequestBuilder<PictureDrawable>
            ) {
                if (item !is CoveredAndExceptionModel.Title) {
                    invalid(item)
                    return
                }
                binding.root.text = item.text
            }
        }

        class Description(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.peril_description)) {
            private val binding by viewBinding(PerilDescriptionBinding::bind)
            override fun bind(
                item: CoveredAndExceptionModel,
                requestBuilder: RequestBuilder<PictureDrawable>
            ) {
                if (item !is CoveredAndExceptionModel.Description) {
                    invalid(item)
                    return
                }
                binding.root.text = item.text
            }
        }
    }
}

