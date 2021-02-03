package com.hedvig.app.feature.onboarding.ui

import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnStart
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.GenericErrorBinding
import com.hedvig.app.databinding.PlanCardBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.onboarding.ChoosePlanViewModel
import com.hedvig.app.feature.onboarding.OnboardingDiffUtilCallback
import com.hedvig.app.feature.onboarding.OnboardingModel
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity.Companion.COMBO
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity.Companion.CONTENTS
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity.Companion.TRAVEL
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.doOnEnd
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.invalidData

class OnboardingAdapter(
    val viewModel: ChoosePlanViewModel,
    val marketProvider: MarketManager
) :
    ListAdapter<OnboardingModel, OnboardingAdapter.ViewHolder>(OnboardingDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.plan_card -> ViewHolder.QuoteType(parent)
        R.layout.generic_error -> ViewHolder.Error(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is OnboardingModel.Bundle -> R.layout.plan_card
        OnboardingModel.Error -> R.layout.generic_error
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel, marketProvider)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
                item: OnboardingModel,
                viewModel: ChoosePlanViewModel,
                marketProvider: MarketManager,
        )

        class QuoteType(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.plan_card)) {
            private val binding by viewBinding(PlanCardBinding::bind)
            override fun bind(
                    item: OnboardingModel,
                    viewModel: ChoosePlanViewModel,
                    marketProvider: MarketManager,
            ) {
                if (item !is OnboardingModel.Bundle) {
                    invalidData(item)
                    return
                }

                binding.apply {
                    radioButton.isChecked = false
                    container.setBackgroundColor(container.context.colorAttr(android.R.attr.colorBackground))
                    discount.remove()
                    blur.remove()

                    if (item.selected) {
                        val name = item.embarkStory.name
                        container.setBackgroundResource(
                            when {
                                name.contains(COMBO) -> R.drawable.gradient_summer_sky
                                name.contains(CONTENTS) -> R.drawable.gradient_fall_sunset
                                name.contains(TRAVEL) -> R.drawable.gradient_spring_fog
                                else -> R.drawable.gradient_fall_sunset
                            }
                        )
                        blur.show()
                        blur.setColorFilter(
                            blur.context.compatColor(
                                when {
                                    name.contains(COMBO) -> R.color.blur_summer_sky
                                    name.contains(CONTENTS) -> R.color.blur_fall_sunset
                                    name.contains(TRAVEL) -> R.color.blur_spring_fog
                                    else -> R.color.blur_fall_sunset
                                }
                            )
                        )
                        radioButton.isChecked = true
                    }
                    val discountMetadata =
                        item.embarkStory.metadata.find { it.asEmbarkStoryMetadataEntryDiscount != null }
                    if (discountMetadata != null) {
                        discount.show()
                        discount.text =
                            discountMetadata.asEmbarkStoryMetadataEntryDiscount?.discount
                    }
                    name.text = item.embarkStory.title
                    description.text = item.embarkStory.description
                    root.setHapticClickListener {
                        animate(root.width.toFloat() + shimmer.width.toFloat())
                        viewModel.setSelectedQuoteType(item.copy(selected = true))
                    }
                }
            }

            private fun animate(distance: Float) {
                binding.apply {
                    val shimmerStartPosition = 0f - shimmer.width
                    Handler(Looper.getMainLooper()).postDelayed({
                        ValueAnimator.ofFloat(shimmerStartPosition, distance).apply {
                            duration = 1000

                            addUpdateListener { animation ->
                                shimmer.translationX = animation.animatedValue as Float
                            }
                            doOnStart {
                                shimmer.visibility = View.VISIBLE
                            }
                            doOnEnd {
                                shimmer.translationX = shimmerStartPosition
                                shimmer.visibility = View.INVISIBLE
                            }
                            start()
                        }
                    },
                        300
                    )
                }
            }
        }

        class Error(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.generic_error)) {
            private val binding by viewBinding(GenericErrorBinding::bind)
            override fun bind(
                item: OnboardingModel,
                viewModel: ChoosePlanViewModel,
                marketProvider: MarketManager
            ) {
                binding.retry.setHapticClickListener { viewModel.load() }
            }
        }
    }
}

