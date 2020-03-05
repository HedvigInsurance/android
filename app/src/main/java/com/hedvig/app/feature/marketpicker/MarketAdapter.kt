package com.hedvig.app.feature.marketpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.feature.language.LanguageAndMarketViewModel
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.market_item.view.*

class MarketAdapter(private val model: LanguageAndMarketViewModel, private val marketId: Int) :
    RecyclerView.Adapter<MarketAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount() = 2

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (marketId == position) {
            selectMarket(holder, position)
        }
        when (position) {
            SV -> holder.itemView.apply {
                flag.setImageDrawable(context.compatDrawable(R.drawable.ic_flag_se))
                country.text = holder.itemView.context.getText(R.string.sweden)
            }
            NO -> holder.itemView.apply {
                flag.setImageDrawable(context.compatDrawable(R.drawable.ic_flag_no))
                country.text = holder.itemView.context.getText(R.string.norway)
            }
        }

        if (position == 0 && holder.itemView.radioButton.isChecked) {
            model.marketLastChecked.postValue(holder.itemView.radioButton)
            model.marketLastCheckedPos.postValue(0)
        }

        holder.itemView.setHapticClickListener {
            selectMarket(holder, position)
        }
    }

    fun getSelectedMarket() = model.marketLastCheckedPos.value

    private fun selectMarket(holder: ViewHolder, position: Int) {
        when (position) {
            SV -> {
                model.updateMarket(Market.SE)
            }
            NO -> {
                model.updateMarket(Market.NO)
            }
        }

        val rb = holder.itemView.radioButton
        rb.isChecked = true
        rb.background = rb.context.getDrawable(R.drawable.ic_radio_button_checked)
        animateRadioButton(holder)
        if (rb.isChecked) {
            model.marketLastChecked.value?.let { rb ->
                if (model.marketLastCheckedPos.value != position) {
                    rb.background =
                        rb.context.getDrawable(R.drawable.ic_radio_button_unchecked)
                    rb.isChecked = false
                    animateRadioButton(holder)
                }
            }
            model.marketLastChecked.postValue(rb)
            model.marketLastCheckedPos.postValue(position)
        } else model.marketLastChecked.postValue(null)
    }

    private fun animateRadioButton(holder: ViewHolder) {
        holder.itemView.radioButton.apply {
            scaleX = 0f
            scaleY = 0f
            spring(
                SpringAnimation.SCALE_X,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(1f)
            spring(
                SpringAnimation.SCALE_Y,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(1f)
        }
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.market_item,
            parent,
            false
        )
    )

    companion object {
        private const val SV = 0
        private const val NO = 1
    }
}
